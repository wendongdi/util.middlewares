package wdd.utils.commons;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 压缩成ZIP 方法1
     *
     * @param srcDir           压缩文件夹路径
     * @param out              压缩文件输出流
     * @param keepdirstructure 是否保留原来的目录结构,true:保留目录结构;
     *                         <p>
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */

    public static void toZip(String srcDir, OutputStream out, boolean keepdirstructure)
            throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), keepdirstructure);
            long end = System.currentTimeMillis();
//            System.out.println("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 压缩成ZIP 方法2
     *
     * @param out      压缩文件输出流
     * @param srcFiles 需要压缩的文件列表
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */

    public static void toZip(OutputStream out, File... srcFiles) throws RuntimeException {
        toZip(Arrays.asList(srcFiles), out);
    }

    public static void toZip(List<File> srcFiles, OutputStream out) throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
//            System.out.println("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param keepdirstructure 是否保留原来的目录结构,true:保留目录结构;
     *                         <p>
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean keepdirstructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepdirstructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (keepdirstructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), keepdirstructure);
                    } else {
                        compress(file, zos, file.getName(), keepdirstructure);
                    }
                }
            }
        }
    }

    public static File zipStrings(String prefix, List<String> strs) throws IOException {
        File fileIn = File.createTempFile(prefix, ".txt");
        File fileZip = File.createTempFile(prefix, ".zip");
        FileWriter fw = new FileWriter(fileIn);
        for (String did : strs) {
            fw.write(did + "\n");
        }
        fw.close();
        FileOutputStream fos2 = new FileOutputStream(fileZip);
        ZipUtil.toZip(fos2, fileIn);
        fos2.close();

        fileIn.delete();
        return fileZip;
    }

    public static void main(String[] args) throws Exception {
        List<String> dids = new ArrayList<>();
        int count = 0;
        while (1000000 > count) {
            count++;
            Collections.addAll(dids, "52FA7A6B3DE6DE161F850A0675B35FA1,1022D8CA9E02FD3A976310AADFDEF03D,659D8734652CEF8062205DE92904DE51,FAD5F8FA23247CF6BB9DAADD97CAEECB,4BEAAC8CA5B516E35BDEDB24F9D6B40D,BFB37072728E0289D5CE3B82B8D4113D,75055BF468A87DBB9990DDE025B1A632,0106D2566640488410E3CBAFA3AF59F8,3BC1D948A09FE245044C44F91BB38AF0,B50B2E6B94B37715E5B360962DB39079,F0351A0EB630830BB1AA08AF9CD9B620,D655F48CC5A6274E0DA304DB3657F63A,4AC859BC4011DC533AAC27372E21D838,21FDA9ACCC939AE61AD7344D57D9F743".split(","));
        }
        System.out.println(dids.size());
        File fileZip = zipStrings("dids", dids);
        System.out.println("fileZip path: " + fileZip.getAbsolutePath());
//        fileZip.delete();
    }


}
