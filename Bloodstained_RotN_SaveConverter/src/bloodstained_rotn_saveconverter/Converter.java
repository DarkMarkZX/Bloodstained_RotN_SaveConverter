package bloodstained_rotn_saveconverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DarkMarkZX
 */
public class Converter
{
    private static final String DATA_FOLDER = "data";
    private static final String KEY_FILE = "key.dat";
    private static final String STEAM_DLC = "Steam_DLC.dat";
    private static final String GOG_DLC = "GOG_DLC.dat";
    
    private byte[] readBinaryFile(Path filepath)
    {
        System.out.println("Reading file: "+filepath.toString());
        byte[] result = null;
        try
        {
            result = Files.readAllBytes(filepath);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private Path writeBinaryFile(Path filepath, byte[] data)
    {
        System.out.println("Writing file: "+filepath.toString());
        Path result = null;
        try
        {
            File parentFolder = filepath.getParent().toFile();
            if (!parentFolder.exists())
            {
                parentFolder.mkdirs();
            }
            result = Files.write(filepath, data);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private byte[] encrypt(byte[] key, Path srcPath)
    {
        if (key == null)
        {
            return null;
        }
        
        byte[] inFile = readBinaryFile(srcPath);
        for (int i=0; i<inFile.length; i++)
        {
            int keyIndex = i % key.length;
            inFile[i] ^= key[keyIndex];
        }
        
        return inFile;
    }
    
    private int indexOf(byte[] data, byte[] pattern)
    {
        for (int i = 0; i < data.length; i++)
        {
            if ((data.length - i) < pattern.length)
            {
                return -1;
            }
            
            for (int j = 0; j < pattern.length; j++)
            {
                if (data[i+j] != pattern[j])
                {
                    break;
                }
                else if (j == pattern.length - 1)
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private String byteToHex(byte b)
    {
        return String.format("%02X", b);
    }
    
    private byte[] fixDlcs(byte[] decryptedFile, byte[] srcDlcs, byte[] dstDlcs)
    {
        int sizeDifference = dstDlcs.length - srcDlcs.length;
        System.out.println(" sizeDifference = " + sizeDifference);
        
        int srcDlcsStart = indexOf(decryptedFile, srcDlcs);
        if (srcDlcsStart < 0)
        {
            System.err.println("Error: could not find provided DLC data in the processed save file.");
            return null;
        }
        int srcDlcsEnd = srcDlcsStart + srcDlcs.length;
        int fixedFileSize = decryptedFile.length + sizeDifference;
        System.out.println(" decryptedFile size = " + decryptedFile.length);
        System.out.println(" fixedFileSize size = " + fixedFileSize);
        byte[] fixedFile = new byte[fixedFileSize];
        
        for (int i=0; i<srcDlcsStart; i++)
        {
            fixedFile[i] = decryptedFile[i];
        }
        for (int i=0; i<dstDlcs.length; i++)
        {
            fixedFile[srcDlcsStart+i] = dstDlcs[i];
        }
        for (int i=0; i<(decryptedFile.length - srcDlcsEnd); i++)
        {
            fixedFile[srcDlcsStart+dstDlcs.length+i] = decryptedFile[srcDlcsStart+srcDlcs.length+i];
        }
        
        byte[] StructProperty_ = {0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x50, 0x72, 0x6F, 0x70, 0x65, 0x72, 0x74, 0x79, 0x00};
        int index = indexOf(fixedFile, StructProperty_);
        if (index >= 0)
        {
            int structSizeIndex = index + StructProperty_.length;
            System.out.println(" old StructProperty = " + byteToHex(fixedFile[structSizeIndex]));
            fixedFile[structSizeIndex] += sizeDifference;
            System.out.println(" new StructProperty = " + byteToHex(fixedFile[structSizeIndex]));
        }
        
        return fixedFile;
    }
    
    private byte[] generateChecksum(byte[] data)
    {
        byte[] result = null;
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("MD5");
            result = md.digest(data);
        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private void fixChecksum(byte[] encryptedFile)
    {
        byte[] contents = Arrays.copyOfRange(encryptedFile, 0, encryptedFile.length - 16);
        byte[] checksum = generateChecksum(contents);
        for (int i=0; i<checksum.length; i++)
        {
            encryptedFile[encryptedFile.length - checksum.length + i] = checksum[i];
        }
    }
    
    public void encryptFile(String srcPath, String dstPath)
    {
        byte[] key = readBinaryFile(Paths.get(DATA_FOLDER, KEY_FILE));
        byte[] result = encrypt(key, Paths.get(srcPath));
        writeBinaryFile(Paths.get(dstPath), result);
    }
    
    public void fixDecryptedFile(String srcFilepath, String srcDlcsPath, String dstDlcsPath)
    {
        byte[] srcFile = readBinaryFile(Paths.get(srcFilepath));
        byte[] srcDlcs = readBinaryFile(Paths.get(srcDlcsPath));
        byte[] dstDlcs = readBinaryFile(Paths.get(dstDlcsPath));
        
        System.out.println("Fixing decrypted file: "+srcFilepath);
        byte[] fixedFile = fixDlcs(srcFile, srcDlcs, dstDlcs);
        writeBinaryFile(Paths.get(srcFilepath), fixedFile);
    }
    
    public void fixSteamFile(String srcFilepath)
    {
        fixDecryptedFile(srcFilepath, DATA_FOLDER+"/"+GOG_DLC, DATA_FOLDER+"/"+STEAM_DLC);
    }
    
    public void fixGogFile(String srcFilepath)
    {
        fixDecryptedFile(srcFilepath, DATA_FOLDER+"/"+STEAM_DLC, DATA_FOLDER+"/"+GOG_DLC);
    }
    
    public void fixFileChecksum(String srcPath)
    {
        byte[] inFile = readBinaryFile(Paths.get(srcPath));
        fixChecksum(inFile);
        writeBinaryFile(Paths.get(srcPath), inFile);
    }
    
    public void convertFile(String srcPath, String dstPath, String srcDlcsPath, String dstDlcsPath)
    {
        encryptFile(srcPath, dstPath);
        fixDecryptedFile(dstPath, srcDlcsPath, dstDlcsPath);
        encryptFile(dstPath, dstPath);
        fixFileChecksum(dstPath);
    }
    
    public void convertGogToSteam(String srcPath, String dstPath)
    {
        convertFile(srcPath, dstPath, DATA_FOLDER+"/"+GOG_DLC, DATA_FOLDER+"/"+STEAM_DLC);
    }
    
    public void convertSteamToGog(String srcPath, String dstPath)
    {
        convertFile(srcPath, dstPath, DATA_FOLDER+"/"+STEAM_DLC, DATA_FOLDER+"/"+GOG_DLC);
    }
    
    public void printHelp()
    {
        System.out.println("Convert encrypted save from GOG to Steam format:");
        System.out.println(" Converter -gog2steam input_file.sav output_file.sav");
        System.out.println();
        
        System.out.println("Convert encrypted save from Steam to GOG format:");
        System.out.println(" Converter -steam2gog input_file.sav output_file.sav");
        System.out.println();
        
        System.out.println("Decrypt / encrypt file:");
        System.out.println(" Converter -encrypt input_file.sav output_file.sav");
        System.out.println();
        
        System.out.println("Convert decrypted save from GOG to Steam format:");
        System.out.println(" Converter -gog2steam_dec input_file.sav output_file.sav");
        System.out.println();
        
        System.out.println("Convert decrypted save from Steam to GOG format:");
        System.out.println(" Converter -steam2gog_dec input_file.sav output_file.sav");
        System.out.println();
        
        System.out.println("Recalculate checksum of encrypted save file:");
        System.out.println(" Converter -checksum encrypted_file.sav");
        System.out.println();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Converter converter = new Converter();
        
        if (args.length <= 0)
        {
            converter.printHelp();
            return;
        }
        
        try
        {
            switch(args[0])
            { 
                case "-gog2steam":
                    converter.convertGogToSteam(args[1], args[2]);
                    break;
                case "-steam2gog":
                    converter.convertSteamToGog(args[1], args[2]);
                    break;
                case "-encrypt":
                    converter.encryptFile(args[1], args[2]);
                    break;
                case "-gog2steam_dec":
                    converter.fixSteamFile(args[1]);
                    break;
                case "-steam2gog_dec":
                    converter.fixGogFile(args[1]);
                    break;
                case "-checksum":
                    converter.fixFileChecksum(args[1]);
                    break;
                default: 
                    converter.printHelp();
            } 
        }
        catch (Exception ex)
        {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            converter.printHelp();
        }
    }
    
}
