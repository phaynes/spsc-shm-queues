package vn.tech;

public class OSUtility {
    public  enum OSTYPE {
        OS_UNKNOWN,
        OS_MAC,
        OS_LINUX,
        OS_SOLARIS,
        OS_WINDOW
    }
    private static OSUtility current;
    final public OSTYPE osType;

    public OSUtility(OSTYPE osType) {
        this.osType = osType;
    }
   
    public static OSUtility getCurrent()  {
        if (current == null)
            current = new OSUtility(osTypeFromName(System.getProperty("os.name").toLowerCase()));
        return current;
    }
    
    static OSTYPE osTypeFromName(String name)
    {
        String osName = name.toLowerCase();
        final boolean isWindows = (osName.indexOf("win") >= 0);
        final boolean isLinux = (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0);
        final boolean isMac = (osName.indexOf("mac") >= 0);
        final boolean isSolaris = (osName.indexOf("sunos") >= 0);

        if (isWindows)
            return OSTYPE.OS_WINDOW;
        else if (isMac)
            return OSTYPE.OS_MAC;
        else if (isLinux)
            return OSTYPE.OS_LINUX;
        else if (isSolaris)
            return OSTYPE.OS_SOLARIS;
        else
            return OSTYPE.OS_UNKNOWN;
    }

}
