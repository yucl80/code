
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public final class WeblogicCollector {
	private static final Log log = LogFactory.getLog(WeblogicCollector.class);

	private static final ClassLoader cl;

	private static Method getCurrentSubject;

	private static Method runAs;

	private static Constructor objectNameConstructor;

	private static Method getAttributes;

	private static Method getValue;

	private static Class InstanceNotFoundException;

	static {
		try {
			cl = new WeblogicClassLoader(Util.getLibPath("weblogic8"),
					WeblogicCollector.class.getClassLoader());
			Class Security = cl.loadClass("weblogic.security.Security");
			getCurrentSubject = Security.getMethod("getCurrentSubject",
					(Class[]) null);
			runAs = Security.getMethod("runAs", new Class[] { Subject.class,
					PrivilegedAction.class });
			Class MBeanServer = cl.loadClass("javax.management.MBeanServer");
			Class ObjectName = cl.loadClass("javax.management.ObjectName");
			InstanceNotFoundException = cl
					.loadClass("javax.management.InstanceNotFoundException");
			objectNameConstructor = ObjectName
					.getConstructor(new Class[] { java.lang.String.class });
			getAttributes = MBeanServer.getMethod("getAttributes", new Class[] {
					ObjectName, java.lang.String[].class });
			Class Attribute = cl.loadClass("javax.management.Attribute");
			getValue = Attribute.getMethod("getValue", (Class[]) null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public final static class WeblogicClassLoader extends URLClassLoader {
		protected synchronized Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			Class c = findLoadedClass(name);
			if (c == null)
				if (name.indexOf("javax.management") >= 0
						|| name.indexOf("weblogic") >= 0)
					try {
						try {
							c = findClass(name);
						} catch (SecurityException se) {
							int i = name.lastIndexOf('.');
							String pkgname = name.substring(0, i);
							Package pkg = getPackage(pkgname);
							if (pkg == null)
								definePackage(pkgname, null, null, null, null,
										null, null, null);
						}
						if (resolve)
							resolveClass(c);
					} catch (ClassNotFoundException cnfe) {
						c = super.loadClass(name, resolve);
					}
				else
					c = super.loadClass(name, resolve);
			return c;
		}

		public WeblogicClassLoader(URL urls[], ClassLoader parent) {
			super(urls, parent);
		}
	}

	public final static JMXConnection getJMXConnection(ConnProp prop)
			throws Exception {
		ClassLoader originalLoader = Thread.currentThread()
				.getContextClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		Context ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.PROVIDER_URL, (String) prop.get("url"));
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"weblogic.jndi.WLInitialContextFactory");
			env.put(Context.SECURITY_PRINCIPAL, (String) prop.get("username"));
			env
					.put(Context.SECURITY_CREDENTIALS, (String) prop
							.get("password"));
			ctx = new InitialContext(env);
			Subject subject = (Subject) getCurrentSubject.invoke(null,
					(Object[]) null);
			Object home = ctx.lookup("weblogic.management.home.localhome");
			Method getMBeanServer = home.getClass().getMethod("getMBeanServer",
					new Class[0]);
			Object mbeanServer = getMBeanServer.invoke(home, (Object[]) null);
			JMXConnection conn = new JMXConnection(mbeanServer, subject);
			return conn;
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
			Thread.currentThread().setContextClassLoader(originalLoader);
		}
	}

	public final static ArrayList collect(final MonitorItem item, int job_id) {
		ClassLoader originalLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(cl);
			if (item.getConnection() == null) {
				JMXConnection conn = getJMXConnection(JobDao
						.getMonConnProp(job_id));
				item.setConnection(conn);
			}
			Object alertList = runAs.invoke(null, item.getConnection()
					.getSubject(), new PrivilegedAction() {
				public Object run() {
					ArrayList alerts = null;
					MonitorObject[] objs = item.getMonitorObject();
					for (int objectIndex = 0; objectIndex < objs.length; objectIndex++) {
						try {
							long ctime = System.currentTimeMillis();
							ArrayList attributelist = (ArrayList) (getAttributes
									.invoke(item.getConnection()
											.getMbeanServer(),
											objs[objectIndex].getObjectName(),
											objs[objectIndex]
													.getAttributeNames()));
							int attributesSize = objs[objectIndex]
									.getAttributeNames().length;
							if (attributesSize != attributelist.size()) {
								continue;
							}
							for (int i = 0; i < attributesSize; i++) {
								MonAttribute attr = objs[objectIndex]
										.getAttributes()[i];
								attr.setValid(false);
								long lastTime = ctime - attr.getDctime();
								attr.setDctime(ctime);
								char valueType = attr.getValueType();
								Object attribute = attributelist.get(i);
								Object value = getValue.invoke(attribute,
										(Object[]) null);
								if (attr.getFilter() != null) {
									try {
										ValueFilter filter = (ValueFilter) Class
												.forName(attr.getFilter())
												.newInstance();
										value = filter.doFilter(value);
									} catch (ClassNotFoundException e) {
										log.error("filter class not found! "
												+ attr.getFilter());
									}
								}
								if (value == null) {
									StringBuilder sb = new StringBuilder();
									sb.append("resource id: ").append(
											attr.getId()).append(
											" invalid value");
									log.warn(sb.toString());
									continue;
								} else if (valueType == 'S') {
									attr.setValue(value.toString());
									attr.setValid(true);									
								} else {
									try {
										if (valueType == 'N') {
											attr.setValue(new Double(value
													.toString()));
											attr.setValid(true);
										} else if (valueType == 'T') {
											double v = Double.parseDouble(value
													.toString());
											Object lastValue = attr
													.getLastValue();
											attr.setLastValue(v);
											if (lastValue != null) {
												v = (v - Double
														.parseDouble(lastValue
																.toString()))
														/ lastTime * 60000;
												if (v >= 0) {
													attr.setValue(String
															.valueOf(v));
													attr.setValid(true);
												}
											}
										}
									} catch (NumberFormatException e) {
										StringBuilder sb = new StringBuilder();
										sb.append("resource id: ").append(
												attr.getId()).append(
												" invalid value");
										log.warn(sb.toString());
										continue;
									}
								}
								if (attr.getState() != null
										&& attr.getState().getThreshold() != null
										&& attr.isValid()) {
									AlertInfo alert = attr.getState()
											.getThreshold().doMatch(
													attr.getState(),
													attr.getValue(),
													attr.getState()
															.getLastValue());
									attr.getState().setLastValue(value);
									if (alert != null) {
										alert.setId(attr.getId());
										alert.setTime(ctime);
										alert
												.setCause(objs[objectIndex]
														.getAttributeNames()[i]
														+ " = "
														+ String.valueOf(value));
										if (alerts == null) {
											alerts = new ArrayList();
										}
										alerts.add(alert);
									}
								}
							}
						} catch (Exception e) {
							if (InstanceNotFoundException.isInstance(e
									.getCause())) {
								log.error("InstanceNotFoundException :"
										+ objs[objectIndex].getObjectName());
							} else {
								item.setConnection(null);
								log.error(e.getMessage(), e);
								return null;
							}
						}
					}
					return alerts;
				}
			});
			item.jobState.setState(null);
			return (ArrayList) alertList;
		} catch (Exception e) {
			String state = e.getClass().getSimpleName();
			item.jobState.setState(state);
			log.error("job id:" + job_id, e);
			return null;
		} finally {
			Thread.currentThread().setContextClassLoader(originalLoader);
		}
	}

	public static final Object newObjectName(String objectName)
			throws Exception {
		Object oname = null;
		try {
			oname = objectNameConstructor
					.newInstance(new Object[] { objectName });
			return oname;
		} catch (Exception e) {
			throw new MalformedObjectNameException(objectName);
		}
	}
}
