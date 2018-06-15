# code
FakeDNS

https://git1-us-west.apache.org/repos/asf?p=kudu.git;a=blob;f=java/kudu-client/src/test/java/org/apache/kudu/client/FakeDNS.java;hb=HEAD



https://bugs.openjdk.java.net/browse/JDK-8176361
Previous JDK releases documented how to configure `java.net.InetAddress` to use the JNDI DNS service provider as the name service. This mechanism, and the system properties to configure it, have been removed in JDK 9 
  
  
A new mechanism to configure the use of a hosts file has been introduced. 
  
A new system property `jdk.net.hosts.file` has been defined. When this system property is set, the name and address resolution calls of `InetAddress`, i.e `getByXXX`, retrieve the relevant mapping from the specified file. The structure of this file is equivalent to that of the `/etc/hosts` file. 
  
When the system property `jdk.net.hosts.file` is set, and the specified file doesn't exist, the name or address lookup will result in an UnknownHostException. Thus, a non existent hosts file is handled as if the file is empty.
