package uk.co.leopardsoftware.firsttest;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.*;
import java.io.File;

/**
 * Created by pdrage on 14/04/2014.
 * from example source at http://www.jcraft.com/jsch/examples/Sftp.java.html
 */
public class MySFTPClient {

        //Now, declare a public FTP client object.

        private static final String TAG = "MySFTPClient";

        private Session session = null;
        ChannelSftp c = null;

    //Method to connect to FTP server:
        public boolean ftpConnect(String host, String username,
                                  String password, int port)
        {

            try {
                JSch jsch = new JSch();

                session = jsch.getSession(username, host, port);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);

                jsch.addIdentity("/storage/emulated/0/uk.co.leopardsoftware/id_rsa", "pDr46@.2012");

                Log.i(TAG, "attempting to connect with " + username + ", " + password + ", " +host+ ", " + port);
                session.connect();
                Log.i(TAG, "connected?");

                Channel channel = session.openChannel("sftp");
                channel.connect();
                c = (ChannelSftp) channel;


            }
            catch(Exception e){
                System.out.println(e);
                return false;
            }
            return true;
        }

        //Method to disconnect from FTP server:

        public boolean ftpDisconnect()
        {
            try {
                session.disconnect();
            } catch (Exception e) {
                Log.i(TAG, "Error occurred while disconnecting from ftp server.");
            }

            return false;
        }

        //Method to get current working directory:

        public String ftpGetCurrentWorkingDirectory()
        {
            return null;
        }

        //Method to change working directory:

        public boolean ftpChangeDirectory(String directory_path)
        {
            return false;
        }

        //Method to list all files in a directory:

        public void ftpPrintFilesList(String dir_path)
        {
             return ;
        }

        //Method to create new directory:

        public boolean ftpMakeDirectory(String new_dir_path)
        {
           return false;
        }

        //Method to delete/remove a directory:

        public boolean ftpRemoveDirectory(String dir_path)
        {
            return false;
        }

        //Method to delete a file:

        public boolean ftpRemoveFile(String filePath)
        {
            return false;
        }

        //Method to rename a file:

        public boolean ftpRenameFile(String from, String to)
        {
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
                int mode=ChannelSftp.OVERWRITE;

                Log.e(TAG, "Attempting upload of  " + srcFilePath + " into " +  desDirectory+"/"+desFileName);
                File f = new File(srcFilePath);
                c.put(srcFilePath, desDirectory+"/"+desFileName, null, mode);

                c.put("/dev/null/", desDirectory+"/"+desFileName + ".completed", null, mode);
                return true;
            }
            catch (Exception e) {
                Log.i(TAG, "upload failed: " + e);
            }

            return false;
        }



}

