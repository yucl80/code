
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class Util {
	private static final Log log = LogFactory.getLog(Util.class);

	private static final String LIBPATH = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "lib"
			+ System.getProperty("file.separator");

	public static String toChs(String str) {
		if (str == null) {
			return "";
		}
		try {
			return new String(str.getBytes(Config
					.getValue("database.encode","GB18030")));
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}
		return "";
	}

	public static URL[] getLibPath(String pluginName) {
		URL[] urls = new URL[0];
		File fd = new File(LIBPATH + pluginName);		
		File lst = new File(LIBPATH + pluginName+System.getProperty("file.separator")+"liblst");
		if (lst.exists()) {
			try {
				StringBuilder fc = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(lst)));
				String line = null;
				while ((line = reader.readLine()) != null) {
					fc.append(line);
				}
				if (fc.length() > 0) {
					String[] tmp = fc.toString().split(",");
					ArrayList<URL> list = new ArrayList<URL>();
					for (int i = 0; i < tmp.length; i++) {
						try {
							File jf = new File(LIBPATH + pluginName+ System.getProperty("file.separator") + tmp[i].trim());
							if (jf.exists()) {
								list.add(jf.toURL());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					urls = new URL[list.size()];
					urls = list.toArray(urls);
					return urls;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (fd.isDirectory()) {
			File[] f = fd.listFiles();
			urls = new URL[f.length];
			for (int i = 0; i < f.length; i++) {
				try {
					urls[i] = (f[i].toURL());
				} catch (MalformedURLException e) {					
					log.error(e.getMessage(), e);
				}
			}
		}
		return urls;
	}
}
