using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Security.Principal;
using System.Text;
using static System.Net.Mime.MediaTypeNames;
using Microsoft.Win32;
using System.Windows.Forms;
using System.IO;

namespace PolyGlot_Frontend
{
    class Program
    {
        static String extension = ".pgd";
        static String fileDescription = "PolyGlot Language Library";
        static String keyName = "PolyGlot_Lang_Lib";
        static String exeFilename = "PolyGlot Frontend.exe";
        static String opensWith = System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName;
        // TODO: Edit below to add additional arguments
        static String commandValue = "\"" + opensWith + "\"" + " \"%1\"";
        static String editString = "edit";
        static String commandString = "command";
        static String shell = "Shell";
        static String commandFile = "cmd.exe";
        static String command = "java -jar ";
        static String baseAppJar = "PolyGlot.jar";

        static void Main(string[] args)
        {
            RegistryKey key;
            String keyCommand;
            
            if (!testJavaInstalled())
            {
                MessageBox.Show("Java is not installed. Please download/install the JVM (https://java.com/download) to use PolyGlot.", "Java Required");
                return;
            }
            
            // minimize system calls...
            key = Microsoft.Win32.Registry.ClassesRoot.OpenSubKey(keyName);
            key = key == null ? null : key.OpenSubKey(shell);
            key = key == null ? null : key.OpenSubKey(editString);
            key = key == null ? null : key.OpenSubKey(commandString);
            keyCommand = key == null ? null : key.GetValue("").ToString();

            // run self with escalated priveleges to set file association if appropriate
            if ((keyCommand == null
                    || !keyCommand.Equals(commandValue))
                && !IsAdministrator())
            {
                // only escalate and exit current thread if user chooses to do so.
                if (MessageBox.Show(".pgd files are not currently configured to open with PolyGlot. Would you like to do so? (permission escalation will appear)",
                    "File Association", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
                {
                    restartEscalated(args);
                    return;
                }
            } else if (keyCommand == null
                || !keyCommand.Equals(commandValue))
            {
                setFileAssociation(key);
            }

            try
            {
                startPolyGlot(args);
            } catch (Exception e)
            {
                MessageBox.Show("Unable to start PolyGlot via front end due to:\n" + e.Message + "\n Please start via jar file.");
            }
        }

        //tests whether Java is installed.
        private static Boolean testJavaInstalled()
        {
            RegistryKey rk = Registry.LocalMachine;
            RegistryKey subKey = rk.OpenSubKey("SOFTWARE\\JavaSoft\\Java Runtime Environment");

            string currentVerion = subKey == null ? "" : subKey.GetValue("CurrentVersion").ToString();

            return !(subKey == null || subKey.Equals(""));
        }

        private static void restartEscalated(string[] args)
        {
            var exeName = System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName;
            ProcessStartInfo startInfo = new ProcessStartInfo(exeName);
            startInfo.Verb = "runas";
            startInfo.Arguments = arrayToString(args);
            System.Diagnostics.Process.Start(startInfo);
        }

        private static void startPolyGlot(string[] args)
        {
            String location = System.Reflection.Assembly.GetExecutingAssembly().Location.Replace(exeFilename, "");
            Process process = new Process();
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = "cmd.exe";
            startInfo.RedirectStandardInput = true;
            startInfo.RedirectStandardOutput = true;
            startInfo.UseShellExecute = false;
            startInfo.CreateNoWindow = true;
            process.StartInfo = startInfo;

            if (!File.Exists(location + baseAppJar))
            {
                throw new Exception("Unable to find " + baseAppJar + ". Frontend must be located in the same directory to run properly.");
            }

            process.Start();

            // TODO: in the future, expand for additional possible arguments
            String finalCommand = command + "\"" + location + baseAppJar + "\"";
            if (args.Length > 0)
            {
                finalCommand += " \"" + args[0] + "\"";
            }

            process.StandardInput.WriteLine(finalCommand);
        }
        
        private static void setFileAssociation(RegistryKey key)
        {
            Microsoft.Win32.RegistryKey BaseKey;
            Microsoft.Win32.RegistryKey shell;
            Microsoft.Win32.RegistryKey currentUser;
            

            BaseKey = Microsoft.Win32.Registry.ClassesRoot.CreateSubKey(extension);
            BaseKey.SetValue("", keyName);

            // only create if not changing existing value
            if (key == null)
            {
                key = Microsoft.Win32.Registry.ClassesRoot.CreateSubKey(keyName);
            }
            else
            {
                // can only open to edit post-priveledge escalation
                key = Microsoft.Win32.Registry.ClassesRoot.OpenSubKey(keyName, true);
            }

            key.SetValue("", fileDescription);
            key.CreateSubKey("DefaultIcon").SetValue("", "\"" + opensWith + "\",1");
            shell = key.CreateSubKey("Shell");
            shell.CreateSubKey("edit").CreateSubKey(commandString).SetValue("", commandValue);
            shell.CreateSubKey("open").CreateSubKey(commandString).SetValue("", commandValue);
            BaseKey.Close();
            key.Close();
            shell.Close();

            currentUser = Microsoft.Win32.Registry.CurrentUser.CreateSubKey(@"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ucs");
            currentUser = currentUser.OpenSubKey("UserChoice", Microsoft.Win32.RegistryKeyPermissionCheck.ReadWriteSubTree, System.Security.AccessControl.RegistryRights.FullControl);

            if (currentUser != null)
            {
                currentUser.SetValue("Progid", keyName, Microsoft.Win32.RegistryValueKind.String);
                currentUser.Close();

                Microsoft.Win32.Registry.CurrentUser.OpenSubKey("Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + extension, true);
                currentUser.DeleteSubKey("UserChoice", false);
                currentUser.Close();
            }

            // Tell explorer the file association has been changed
            SHChangeNotify(0x08000000, 0x0000, IntPtr.Zero, IntPtr.Zero);
        }

        private static string arrayToString(string[] arr)
        {
            string ret = "";

            for (int i = 0; i < arr.Length; i++)
            {
                ret += " " + arr[i];
            }

            return ret;
        }

        private static bool IsAdministrator()
        {
            WindowsIdentity identity = WindowsIdentity.GetCurrent();
            WindowsPrincipal principal = new WindowsPrincipal(identity);
            return principal.IsInRole(WindowsBuiltInRole.Administrator);
        }

        [System.Runtime.InteropServices.DllImport("shell32.dll", CharSet = System.Runtime.InteropServices.CharSet.Auto, SetLastError = true)]
        public static extern void SHChangeNotify(uint wEventId, uint uFlags, IntPtr dwItem1, IntPtr dwItem2);
    }
}
