//package hdfsfile;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.*;
//
//import java.io.IOException;
//import java.util.List;
//
//public class HDFSUtils {
//    private static String hdfsUrl = "hdfs://localhost:9000";
//    private static Configuration conf;
//    static {
//        conf = new Configuration();
//        conf.set("fs.defaultFS",hdfsUrl);
//    }
//    public static void download(String remote,String local) throws IOException {
//        FileSystem fs = FileSystem.get(conf);
//        Path lo = new Path(local);
//        Path re = new Path(remote);
//        fs.copyToLocalFile(re,lo);
//        fs.close();
//    }
//    public static void upload(String local,String remote) throws IOException{
//        FileSystem fs = FileSystem.get(conf);
//        Path lo = new Path(local);
//        Path re = new Path(remote);
//        fs.copyFromLocalFile(lo,re);
//        fs.close();
//    }
//    public static void mkDir(String path) throws IOException{
//        FileSystem fs = FileSystem.get(conf);
//        Path dirPath = new Path(path);
//        boolean m = fs.mkdirs(dirPath);
//        if(m){
//            System.out.println("Create dir success");
//        }else{
//            System.out.println("Create dir failed");
//        }
//        fs.close();
//    }
//    public static List<FileStatus> listDir(Configuration conf, String dir,boolean recursive){
//        return null;
//    }
//    public static List<FileStatus> listFileStatus(Configuration conf, String dir) throws IOException {
//        return null;
//    }
//
//    public static void copyFromLocal(Configuration conf,String localPath,String remotePath) throws IOException {
//        Path  lp = new Path(localPath);
//        Path  rp = new Path(remotePath);
//        FileSystem fs = FileSystem.get(conf);
//        fs.copyFromLocalFile(lp,rp);
//
//    }
//    public static void main(String[] args) throws Exception {
//
////        Configuration conf = new Configuration();
////        conf.set("fs.defaultFS", "hdfs://localhost:9000");
////        FileSystem fs = FileSystem.get(conf);
//        //System.out.println(fs.toString());
//        //fs.copyFromLocalFile(new Path("file:///d:/h.java"), new Path("/input7/Hello4.java"));
//        // fs.copyToLocalFile(new Path("/user/input/DataShuffle2.java"), new Path("e:/DataShuffle.java"));
//        // fs.mkdirs(new Path("/input7"));
//        // 查看文件内容
//		/*FSDataInputStream is = fs.open(new Path("/user/input/Hello3.java"));
//		BufferedReader d = new BufferedReader(new InputStreamReader(is));
//		String line = d.readLine();
//		while (line != null) {
//			System.out.println(line);
//			line = d.readLine();
//		}
//		is.close();*/
//        //fs.close();
//    }
//}
package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HDFSUtils {
    private static String hdfsUrl = "hdfs://localhost:9000";
    private static Configuration conf;

    // 静态块中配置HDFS
    static {
        conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUrl);
    }

    /**
     * 下载文件从HDFS到本地路径
     */
    public static void download(String remote, String local) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path localPath = new Path(local);
            Path remotePath = new Path(remote);
            fs.copyToLocalFile(remotePath, localPath);
        } finally {
            if (fs != null) fs.close();
        }
    }

    /**
     * 上传文件从本地到HDFS路径
     */
    public static void upload(String local, String remote) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path localPath = new Path(local);
            Path remotePath = new Path(remote);
            fs.copyFromLocalFile(localPath, remotePath);
        } finally {
            if (fs != null) fs.close();
        }
    }

    public static void uploadDirectoryRecursively(String localPath, String remotePath) throws IOException {
        File localDir = new File(localPath);
        File[] files = localDir.listFiles();

        // 获取当前目录的名称
        String currentDirName = localDir.getName();
        String newRemotePath = remotePath + "/" + currentDirName; // 保持当前目录

        // 创建远程目录
        FileSystem fs = FileSystem.get(conf);
        Path remoteDirPath = new Path(newRemotePath);

        // 打印调试信息
        System.out.println("Attempting to create directory: " + newRemotePath.toString());

        // 检查目录是否创建成功
        if (!fs.mkdirs(remoteDirPath)) {
            throw new IOException("Failed to create directory: " + newRemotePath);
        }

        if (files != null) {
            for (File file : files) {
                String remoteFilePath = newRemotePath + "/" + file.getName(); // 在新路径下上传文件
                if (file.isDirectory()) {
                    // 递归上传子目录
                    uploadDirectoryRecursively(file.getAbsolutePath(), newRemotePath);
                } else {
                    // 上传文件
                    HDFSUtils.upload(file.getAbsolutePath(), remoteFilePath);
                }
            }
        }
    }


    /**
     * 创建HDFS中的目录
     */
    public static void mkDir(String path) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path dirPath = new Path(path);
            if (fs.mkdirs(dirPath)) {
                System.out.println("Directory created successfully: " + path);
            } else {
                System.out.println("Directory creation failed: " + path);
            }
        } finally {
            if (fs != null) fs.close();
        }
    }

    /**
     * 列出HDFS目录中的文件和子目录
     */
    public static List<FileStatus> listDir(String dir, boolean recursive) throws IOException {
        List<FileStatus> fileStatusList = new ArrayList<>();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            RemoteIterator<LocatedFileStatus> fileStatusIterator = fs.listFiles(new Path(dir), recursive);
            while (fileStatusIterator.hasNext()) {
                fileStatusList.add(fileStatusIterator.next());
            }
        } finally {
            if (fs != null) fs.close();
        }
        return fileStatusList;
    }

    /**
     * 列出文件的状态信息
     */
    public static List<FileStatus> listFileStatus(String dir) throws IOException {
        List<FileStatus> fileStatusList = new ArrayList<>();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            FileStatus[] statuses = fs.listStatus(new Path(dir));
            for (FileStatus status : statuses) {
                fileStatusList.add(status);
            }
        } finally {
            if (fs != null) fs.close();
        }
        return fileStatusList;
    }

    /**
     * 从HDFS读取文件内容
     */
    public static String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        FileSystem fs = null;
        BufferedReader reader = null;
        try {
            fs = FileSystem.get(conf);
            FSDataInputStream inputStream = fs.open(new Path(filePath));
            // 解决中文乱码问题
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } finally {
            if (reader != null) reader.close();
            if (fs != null) fs.close();
        }
        return content.toString();
    }


    public static void delete(String path, boolean recursive) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path hdfsPath = new Path(path);
            if (fs.exists(hdfsPath)) {
                boolean isDeleted = fs.delete(hdfsPath, recursive);
                if (isDeleted) {
                    System.out.println("Deleted successfully: " + path);
                } else {
                    System.out.println("Deletion failed: " + path);
                }
            } else {
                System.out.println("Path does not exist: " + path);
            }
        } finally {
            if (fs != null) fs.close();
        }
    }

    public static boolean isDirectory(String path) throws IOException {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            Path hdfsPath = new Path(path);
            FileStatus status = fs.getFileStatus(hdfsPath);
            return status.isDirectory();
        } finally {
            if (fs != null) fs.close();
        }

    }

    public static void main(String[] args) {
        try {
            // 示例操作
            String remotePath = "/new_directory";
            String localPath = "D:/a";
            //upload(localPath,remotePath);
            uploadDirectoryRecursively(localPath,remotePath);
            // 下载文件示例
            //download(remotePath + "/sample.txt", localPath + "/downloaded_sample.txt");

            // 上传文件示例
            //upload(localPath + "/sample", remotePath + "/sample");
            //delete(remotePath,true);
            // 创建目录示例
            //mkDir(remotePath + "new_directory");

            // 列出目录文件示例
//            List<FileStatus> fileList = listDir(remotePath, true);
//            for (FileStatus fileStatus : fileList) {
//                System.out.println(fileStatus.getPath().toString());
//            }

            // 读取文件内容示例
            //String fileContent = readFileContent(remotePath + "/sample");
            //System.out.println("File Content: \n" + fileContent);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
