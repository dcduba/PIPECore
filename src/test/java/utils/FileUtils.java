package utils;

public class FileUtils {

    public static String fileLocation(String path) {
        String newpath = FileUtils.class.getResource(path).getPath();
        String os = System.getProperty("os.name");
        
        /* This is a quick and dirty fix, as I am not sure what is causing this. For some reason a / is
         * prepended to a full path (e.g. "/C:/path/to/file.txt"), causing it to fail. This dirty fix
         * will remove this slash.
         */
        if(os.startsWith("Windows") && newpath.startsWith("/")) {
        	newpath = newpath.substring(1);
        }
        return newpath;
    }
}
