package uk.co.leopardsofdtware.firsttest;

        import java.io.BufferedInputStream;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;

        import org.apache.commons.net.ftp.*;

        import android.content.Context;
        import android.util.Log;

        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.io.PrintWriter;
        import java.security.KeyManagementException;
        import java.security.KeyStore;
        import java.security.KeyStoreException;
        import java.security.NoSuchAlgorithmException;
        import java.security.UnrecoverableKeyException;
        import java.security.cert.CertificateException;

        import javax.net.ssl.KeyManager;
        import javax.net.ssl.KeyManagerFactory;
        import javax.net.ssl.SSLContext;
        import javax.net.ssl.TrustManager;
        import javax.net.ssl.TrustManagerFactory;

        import org.apache.commons.net.PrintCommandListener;
//        import org.apache.commons.net.ftp.FTPFile;
//        import org.apache.commons.net.ftp.FTPSClient;
//        import org.apache.commons.net.ftp.FTPSSocketFactory;



/**
 * Created by pdrage on 16/01/2014.
 * from https://drivehq.com/file/DFPublishFile.aspx/FileID1252023466/Keyw1t2o9x5oqoj/MyFTPClient.java
 * and http://wiki-android.blogspot.co.uk/2012/12/creating-android-ftp-client-ftp.html
 */
public class MyFTPClient {

        //Now, declare a public FTP client object.

        private static final String TAG = "MyFTPClient";
//        public FTPClient mFTPClient = null;
        public FTPSClient mFTPClient = null;
        private static final String KEYSTORE_PASS = "******"; //yes, really 6*
        private static final String KEYSTORE_FILE_NAME = "storage/emulated/0/uk.co.leopardsoftware/keystore";


    //Method to connect to FTP server:
        public boolean ftpConnect(String host, String username,
                                  String password, int port)
        {
            String protocol = "TLS"; // SSL/TLS

            try {
 // Secure..               mFTPClient = new FTPClient();
                mFTPClient = new FTPSClient(protocol, false);

                mFTPClient.setRemoteVerificationEnabled(false);
                SSLContext sslContext = getSSLContext();
                FTPSSocketFactory sf = new FTPSSocketFactory(sslContext);
                mFTPClient.setSocketFactory(sf);
                mFTPClient.setBufferSize(1000);
                KeyManager keyManager = getKeyManagers()[0];
                TrustManager trustManager = getTrustManagers()[0];
                mFTPClient.setControlEncoding("UTF-8");

                mFTPClient.setKeyManager(keyManager);
                mFTPClient.setTrustManager(trustManager);

                mFTPClient.addProtocolCommandListener(new PrintCommandListener(
                        new PrintWriter(System.out)));

  // end secure




                // connecting to the host
                mFTPClient.connect(host, port);

                // now check the reply code, if positive mean connection success
                if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                    // login using username & password
                    boolean status = mFTPClient.login(username, password);

	            /* Set File Transfer Mode
	             *
	             * To avoid corruption issue you must specified a correct
	             * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
	             * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE
	             * for transferring text, image, and compressed files.
	             */
                    mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                    mFTPClient.enterLocalPassiveMode();

                    return status;
                }
            } catch (IOException e) {
                if (mFTPClient.isConnected()) {
                    try {
                        mFTPClient.disconnect();
                    } catch (IOException f) {
                        // do nothing
                    }
                }
                System.err.println("Could not connect to server.");
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyManagementException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch(Exception e) {
                Log.d(TAG, "Error: could not connect to host " + host );
                Log.d(TAG, "Message = " + e.getMessage());
            }

            return false;
        }

        //Method to disconnect from FTP server:

        public boolean ftpDisconnect()
        {
            try {
                mFTPClient.logout();
                mFTPClient.disconnect();
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Error occurred while disconnecting from ftp server.");
            }

            return false;
        }

        //Method to get current working directory:

        public String ftpGetCurrentWorkingDirectory()
        {
            try {
                String workingDir = mFTPClient.printWorkingDirectory();
                return workingDir;
            } catch(Exception e) {
                Log.d(TAG, "Error: could not get current working directory.");
            }

            return null;
        }

        //Method to change working directory:

        public boolean ftpChangeDirectory(String directory_path)
        {
            try {
                mFTPClient.changeWorkingDirectory(directory_path);
            } catch(Exception e) {
                Log.d(TAG, "Error: could not change directory to " + directory_path);
            }

            return false;
        }

        //Method to list all files in a directory:

        public void ftpPrintFilesList(String dir_path)
        {
            try {
                FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
                int length = ftpFiles.length;

                for (int i = 0; i < length; i++) {
                    String name = ftpFiles[i].getName();
                    boolean isFile = ftpFiles[i].isFile();

                    if (isFile) {
                        Log.i(TAG, "File : " + name);
                    }
                    else {
                        Log.i(TAG, "Directory : " + name);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        //Method to create new directory:

        public boolean ftpMakeDirectory(String new_dir_path)
        {
            try {
                boolean status = mFTPClient.makeDirectory(new_dir_path);
                return status;
            } catch(Exception e) {
                Log.d(TAG, "Error: could not create new directory named " + new_dir_path);
            }

            return false;
        }

        //Method to delete/remove a directory:

        public boolean ftpRemoveDirectory(String dir_path)
        {
            try {
                boolean status = mFTPClient.removeDirectory(dir_path);
                return status;
            } catch(Exception e) {
                Log.d(TAG, "Error: could not remove directory named " + dir_path);
            }

            return false;
        }

        //Method to delete a file:

        public boolean ftpRemoveFile(String filePath)
        {
            try {
                boolean status = mFTPClient.deleteFile(filePath);
                return status;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        //Method to rename a file:

        public boolean ftpRenameFile(String from, String to)
        {
            try {
                boolean status = mFTPClient.rename(from, to);
                return status;
            } catch (Exception e) {
                Log.d(TAG, "Could not rename file: " + from + " to: " + to);
            }

            return false;
        }

        //Method to download a file from FTP server:

        /**
         * mFTPClient: FTP client connection object (see FTP connection example)
         * srcFilePath: path to the source file in FTP server
         * desFilePath: path to the destination file to be saved in sdcard
         */
        public boolean ftpDownload(String srcFilePath, String desFilePath)
        {
            boolean status = false;
            try {
                FileOutputStream desFileStream = new FileOutputStream(desFilePath);;
                status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
                desFileStream.close();

                return status;
            } catch (Exception e) {
                Log.d(TAG, "download failed");
            }

            return status;
        }

        //Method to upload a file to FTP server:

        /**
         * mFTPClient: FTP client connection object (see FTP connection example)
         * srcFilePath: source file path in sdcard
         * desFileName: file name to be stored in FTP server
         * desDirectory: directory path where the file should be upload to
         */
        public boolean ftpUpload(String srcFilePath, String desFileName,
                                 String desDirectory, Context context)
        {
            boolean status = false;
            try {
                // FileInputStream srcFileStream = new FileInputStream(srcFilePath);

//original
//                FileInputStream srcFileStream = context.openFileInput(srcFilePath);

                // load file from srcFile, not Context
                FileInputStream srcFileStream = new FileInputStream(srcFilePath);
                // change working directory to the destination directory
                //if (ftpChangeDirectory(desDirectory)) {
                status = mFTPClient.storeFile(desFileName, srcFileStream);
                //}

                srcFileStream.close();
                return status;
            }
            catch (Exception e) {
                Log.d(TAG, "upload failed: " + e);
            }

            return status;
        }

    private static SSLContext getSSLContext() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, UnrecoverableKeyException, IOException {
        TrustManager[] tm = getTrustManagers();
        System.out.println("Init SSL Context");
        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init(null, tm, null);

        return sslContext;
    }
    private static KeyManager[] getKeyManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE_FILE_NAME), KEYSTORE_PASS.toCharArray());

        KeyManagerFactory tmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(ks, KEYSTORE_PASS.toCharArray());

        return tmf.getKeyManagers();
    }
    private static TrustManager[] getTrustManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE_FILE_NAME), KEYSTORE_PASS.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        return tmf.getTrustManagers();
    }

}
