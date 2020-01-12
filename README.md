Bloodstained: Ritual of the Night PC Save Game Converter

Author: DarkMarkZX


--- INTRODUCTION ---

This program can be used to transfer save files between GOG, Steam and potentially other versions
of Bloodstained RotN. Normally, when you try to load an incompatible save file, the game will
display the following information:

  "The downloadable content required for this save is not available."
  
This is caused by the fact that each version of the game has a different set of DLC packs.
Unfortunately, the information about installed DLCs is embedded inside each save file, causing them
to be incompatible with different versions of the game.
Both PC versions (GOG and Steam) of Bloodstained RotN store the save files in the following
subdirectory of the user folder:

  AppData\Local\BloodstainedRotN\GOG\Saved\SaveGames


--- PROGRAM USAGE ---

The converter requires that the 64-Bit version of the Java SE Runtime Environment 13 (JRE 13)
or newer is installed on your computer. The prebuilt version of the program is located in the
"dist" subdirectory and consists of a .jar file (the application itself) and the "data" folder,
which contains the following files:

  GOG_DLC.dat	-	DLC info extracted from a decrypted GOG save file. The provided .dat file
					contains references to the following DLC packs: DLC_0001, DLC_0003, DLC_0004,
					DLC_0005 and DLC_0006. If your GOG save file mentions a different set of DLCs,
					then you will have to extract this information from your save file (using a hex
					editor, such as HxD) and replace the contents of the "GOG_DLC.dat" file
					provided with this program.
					
  Steam_DLC.dat	-	DLC info extracted from a decrypted Steam save file. The provided .dat file
					contains references to the following DLC packs: DLC_0001, DLC_0002, DLC_0003
					and DLC_0004. If your Steam save file mentions a different set of DLCs, then
					you will have to extract this information from your save file (using a hex
					editor, such as HxD) and replace the contents of the "Steam_DLC.dat" file
					provided with this program.
					
  key.dat		-	Key used for decrypting and encrypting save files. No modifications required.


The program must be run from a Command-line interpreter, such as cmd.exe or Windows PowerShell.
The available commands are listed below:


  java -jar Bloodstained_RotN_SaveConverter.jar -gog2steam input_file.sav output_file.sav
  
    Decrypts save file, replaces GOG DLC data (specified in "data/GOG_DLC.dat") with Steam DLC data
	(specified in "data/Steam_DLC.dat"), encrypts the file and recalculates its checksum.


  java -jar Bloodstained_RotN_SaveConverter.jar -steam2gog input_file.sav output_file.sav
  
    Decrypts save file, replaces Steam DLC data (specified in "data/Steam_DLC.dat") with GOG DLC
	data (specified in "data/GOG_DLC.dat"), encrypts the file and recalculates its checksum.


  java -jar Bloodstained_RotN_SaveConverter.jar -encrypt input_file.sav output_file.sav
  
    Can be used to either decrypt an encrypted save file (for the purpose of hacking the game or
	extracting DLC information) or encrypt a decrypted file (so it may be loaded by the game).


  java -jar Bloodstained_RotN_SaveConverter.jar -gog2steam_dec input_file.sav output_file.sav
  
    Converts a decrypted save from GOG to Steam format, using information provided by the
	"data/GOG_DLC.dat" and "data/Steam_DLC.dat" files.


  java -jar Bloodstained_RotN_SaveConverter.jar -steam2gog_dec input_file.sav output_file.sav
  
    Converts a decrypted save from Steam to GOG format, using information provided by the
	"data/Steam_DLC.dat" and "data/GOG_DLC.dat" files.


  java -jar Bloodstained_RotN_SaveConverter.jar -checksum encrypted_file.sav
  
    Recalculates the checksum of encrypted save file.


Additionally, two sample scripts are provided with the program:


  gog2steam.bat			-	Instructs the program to convert "Story_Slot20.sav" from GOG to Steam
							version.
							
  gog2steam_batch.bat	-	Instructs the program to convert all save files inside the "SaveGames"
							directory from GOG to Steam version.


--- SOURCE CODE ---

Source code is provided in the form of an Apache NetBeans IDE 11 project.
