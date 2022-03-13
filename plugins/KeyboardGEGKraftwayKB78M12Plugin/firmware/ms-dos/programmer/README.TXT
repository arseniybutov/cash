1.   User Guide of Programmable Keyboard Utility 

     K78C.EXE    :   78 keys cherry switch programmable keyboard utility
     K78M.EXE    :   78 keys membrane programmable keyboard utility
     K120C.EXE   :   120 keys cherry switch programmable keyboard utility
     K120M.EXE   :   120 keys membrane programmable keyboard utility
     K128C.EXE   :   128 keys cherry switch programmable keyboard utility

1.0  General
     This utility is an editor to program the programmable keyboard.

     Before starting this utility, connect the standard PC keyboard into
     the pass through keyboard port of keyboard. All the programming
     keystrokes are performed on the standard PC keyboard.
     After accessing the edit mode, Caps Lock, Num Lock and Scroll Lock on
     the standard PC keyboard are turned off.

     When finishing programming the keyboard, the keyboard will
     perform power-on reset.

     After power on, the programmable keyboard will not operate until the
     key (of lock) is firstly turned to the OFF lock position and then
     turned to any other lock position.

     The programmable keyboard uses membrane technology, when multiple
     keys are depressed, in some cases it will cause a nonidentified switch
     closure, the keyboard will then output X'00' to the system.


1.1  Program Function

     The utility can be started with:

     K78C  [-repeat] [-nokb] [-noclick] [-delay=] [-extkb=] [-program=]
     K78M  
     K120C 
     K120M 
     K128C 

     -repeat       -- turn key repeat on.
     -nokb         -- turn external keyboard off.
     -noclick      -- turn key click off.

     -delay=nn     -- define character output delay.
                      delay time = 3 * nn (ms),  1 <= nn <= 15

     -extkb=cc     -- choose country code.
                      cc = US, UK, France, Germany, Spain, Italy

     -program=file -- program the keyboard with the file name.
                      in this case, the utility does not start the
                      edit mode, but program the keyboard directly.


     When the utility is started only with K78C.EXE, it will preset to be:
                                           K78M.EXE
                                           K120C.EXE
                                           K120M.EXE
                                           K128C.EXE
         key repeat off
         external keyboard on
         key click on
         delay = 1
         extkb = us
     and then start the edit mode.

     After startup, the country code is showed in the upper right corner 


1.2  Edit Mode Menu

File    -- Press ALT-F to access the File Menu.
           Use key UP, DOWN to select SubMenu and press ENTER to execute.
Edit    -- Press Alt-E to access the Edit Menu.
           Use key UP, DOWN to select SubMenu and press ENTER to execute.
Program -- Program the keyboard with the current settings.
           Press ALT-P to start.

File SubMenu
Open    -- Open a file that is saved before.
           Press ALT-O to execute.
Save    -- Save current settings to the file opened.
           if no file is opened, settings will be saved to file K78.COD.
                                                                K120.COD
                                                                K128.COD
           Press ALT-S to execute.
Save as -- Save current settings with new file name.
Print   -- Print out the keyboard layout.
Quit    -- Quit the utility.
           Press ALT-X to execute.

Edit SubMenu
Code redefine     -- Assign one key with a single key.
                     Press ALT-C to access this edit mode.
                     Move the highlight to a key position by arrow key. 
                     Press ENTER to edit the key you select.

                     Press a single key to define, 
                     for example, {F1}, {1}, {a}, {Caps Lock}, {Shift}, ...

                     Move the highlight to edit the next key. 
                     Press ESC to quit this edit mode.

Function redefine -- Assign one key with combination keys.
                     Press ALT-U to access this edit mode.
                     Move the highlight to a key position by arrow key.
                     Press ENTER to edit the key you select.

                     Press combination keys to define,
                     for example, {SHIFT+a}, {CTRL+a}, {ALT+a}, ...
                                  {SHIFT+CTRL+a}, {CTRL+ALT+a}, ...
                                  {SHIFT+CTRL+ALT+a}, ...
                     left ALT and right ALT are classified. 

                     Move the highlight to edit the next key. 
                     Press ESC to quit this edit mode.

Key Lock redefine -- Assign one key lock with a single key/combination keys.
                     Press ALT-L to access this edit mode.
                     Input the key lock position number (1-6).

                     Press single key or combination keys to define,
                     for example, {F1}, {ALT+F1}, ...

                     Input the next key lock position number to edit.
                     Press ESC to quit this edit mode.

Key String define -- Assign one key with a string of characters.
                     The maximum number of key string code is 16.

                     Press combination key once to make and press it
                     once again to break.
                     SHIFT make key is represented by "S^".
                     SHIFT break key is represented by "S^break".
                     CTRL make key is represented by "C^".
                     CTRL break key is represented by "C^break".
                     ALT make key is represented by "A^".
                     ALT break key is represented by "A^break".
                     Left ALT and right ALT is classified, but their
                     representation are the same.

                     Press ALT-K to access this edit mode.
                     Move the highlight to a key position by arrow key.
                     Press ENTER to edit the key you select.

                     Type a string of characters to define,
                     for example, if you want to define "ASdfG",
                     press SHIFT, a, s, SHIFT, d, f, SHIFT, g, SHIFT
                     representation : S^ a s S^BREAK d f S^ g S^BREAK
                     number of code : 9

                     Move the highlight to edit the next key. 
                     Press ESC to quit this edit mode.

Test keys         -- After programming the keyboard, use this function to
                     test whether each key is programmed correctly.
                     This test does not test the key string and Null key.
                     Press ALT-T to start.
                     Press each key on the programmable keyboard, the key
                     position of the utility will be highlighted.
                     When finish, press ESC to quit the test.

Test key lock     -- After programming the keyboard, use this function to
                     test whether each key lock is programmed correctly.
                     Press ALT-Y to start.
                     Turn the key to each lock position, the lock position
                     of the utility will be highlighted.
                     When finish, press ESC to quit the test.

Null Key define   -- Assign one key with Null function.
                     When one key is defined as Null key , it is disable.
                     Press ALT-N to access this edit mode
                     Input the key position number.
                     Press ESC to quit this edit mode.


2.   User Guide of Bar Code and MSR Decoder Utility KBD-130.EXE

     KBD-130.EXE is to configure the decoder of bar code reader and
     magnetic card reader.
    
     After accessing the utility, press F1~F4 to switch pages, and press
     the combination keys on the left side to change the configuration.

2.0  General Configuration (Page 1)
     (ALT-B) Time delay of key strokes
          -  define the intercharacter delay, delay = 0~99 ms

     (ALT-C) Sound buzzer when good read
          -  when this option is selected, it will produce an audible beep
             when the read code is good.

     (ALT-D) Country code
          -  select the keyboard country type,
             US, UK, France, Germany, Spain, Italy

     (ALT-E) Type of MSR
          -  select the type of magnetic card reader
             track 1 only, track 2 only, track 3 only, track 1 and 2,
             track 2 and 3, track 1, 2 and 3

2.1  Bar Code Reader Configuration (Page 2, Page 3)
     Page 2
     (ALT-A) Use bar code reader
          -  enable or disable the bar code reader
     
     (ALT-B) Type of scanner
          -  select the type of scanner, trigger or auto detect  

     (ALT-C) Preamble characters
          -  characters sent before the bar code data or MSR data

     (ALT-D) Postamble characters
          -  characters sent after the bar code data or MSR data

     (ALT-E) Send bar code ID
          -  when this option is enable, it will send the ID of bar code
               bar code            ID
               ---------------------------
               Code 39             A
               Interleaved 2/5     B
               Code 128            C
               UPC_E               D
               UPC_A               E
               EAN-8               F
               EAN-13              G
               Codebar             H

     (ALT-F) Use of auto off
          -  when this option is enable, the power of the scanner will be
             turned off when the read code is good.

     (ALT-G) Enable Code39
          -  when this option is enable, it will decode Code 39 bar code

     (ALT-H) Check digit verification
          -  when this option is enable, it will decode Code 39 bar code
             with check digit

     (ALT-I) Send check digit
          -  when this option is enable, it will send Code 39 check digit

     (ALT-J) Use full ASCII
          -  when this option is enable, it will decode full ASCII Code 39

     (ALT-K) Max. length of bar code
          -  define the maximum length of Code 39 bar code

     (ALT-L) Min. length of bar code
          -  define the minimum length of Code 39 bar code

     (ALT-M) Start code
          -  when this option is enable, it will send Code 39 start code

     (ALT-N) Stop code
          -  when this option is enable, it will send Code 39 stop code

     (ALT-O) Enable codebar
          -  when this option is enable, it will decode Codebar bar code

     (ALT-P) Send start/stop code
          -  when this option is enable, it will send Codebar start and
             stop code

     (ALT-Q) Max. length of bar code
          -  define the maximum length of Codebar bar code

     (ALT-R) Min. length of bar code
          -  define the minimum length of Codebar bar code

     (ALT-S) Enable code MSI
          -  when this option is enable, it will decode Code MSI bar code 

     (ALT-T) Send check digit
          -  when this option is enable, it will send Code MSI check digit  

     (ALT-U) Max. length of bar code
          -  define the maximum length of Code MSI bar code

     (ALT-V) Min. length of bar code
          -  define the minimum length of Code MSI bar code

     (ALT-W) Enable code 128
          -  when this option is enable, it will decode Code 128 bar code 

     (ALT-X) Send check digit
          -  when this option is enable, it will send Code 128 check digit  

     (ALT-Y) Max. length of bar code
          -  define the maximum length of Code 128 bar code

     (ALT-Z) Min. length of bar code
          -  define the minimum length of Code 128 bar code

     Page 3
     (ALT-A) Enable interleaved 2/5
          -  when this option is enable, it will decode interleaved 2/5
             bar code

     (ALT-B) Check digit verification
          -  when this option is enable, it will decode Interleaved 2/5
             bar code with check digit

     (ALT-C) Send check digit
          -  when this option is enable, it will send Interleaved 2/5
             check digit

     (ALT-D) Max. length of bar code
             define the maximum length of Interleaved 2/5 bar code

     (ALT-E) Min. length of bar code
          -  define the minimum length of Interleaved 2/5 bar code

     (ALT-F) Clear leading zero
          -  when this option is enable, it will not send Interleaved 2/5
             leading zero

     (ALT-G) Enable code 11
          -  when this option is enable, it will decode Code 11 bar code

     (ALT-H) Check digit verification
          -  when this option is enable, it will decode Code 11 bar code
             with check digit

     (ALT-I) Send check digit
          -  when this option is enable, it will send Code 11 check digit

     (ALT-J) Max. length of bar code
             define the maximum length of Code 11 bar code

     (ALT-K) Min. length of bar code
          -  define the minimum length of Code 11 bar code

     (ALT-L) Enable UPC-E
          -  when this option is enable, it will decode UPC-E bar code 

     (ALT-M) Truncate leading digits
          -  when this option is enable, it will truncate UPC-E leading digits  

     (ALT-N) Truncate ending digits
          -  when this option is enable, it will truncate UPC-E ending digits  

     (ALT-O) Use supplemental digits
          -  when this option is enable, it will check UPC-E supplemental
             digits

     (ALT-P) Enable UPC-A
          -  when this option is enable, it will decode UPC-A bar code 

     (ALT-Q) Truncate leading digits
          -  when this option is enable, it will truncate UPC-A leading digits  

     (ALT-R) Truncate ending digits
          -  when this option is enable, it will truncate UPC-A ending digits  

     (ALT-S) Use supplemental digits
          -  when this option is enable, it will check UPC-A supplemental
             digits

     (ALT-T) Enable EAN-8
          -  when this option is enable, it will decode EAN-8 bar code 

     (ALT-U) Truncate leading digits
          -  when this option is enable, it will truncate EAN-8 leading digits  

     (ALT-V) Truncate ending digits
          -  when this option is enable, it will truncate EAN-8 ending digits  

     (ALT-W) Use supplemental digits
          -  when this option is enable, it will check EAN-8 supplemental
             digits

     (ALT-X) Enable EAN-13
          -  when this option is enable, it will decode EAN-13 bar code 

     (ALT-Y) Truncate leading digits
          -  when this option is enable, it will truncate EAN-13 leading digits  

     (ALT-Z) Truncate ending digits
          -  when this option is enable, it will truncate EAN-13 ending digits  

     (ALT-1) Use supplemental digits
          -  when this option is enable, it will check EAN-13 supplemental
             digits

2.2  Magnetic Card Reader Configuration (Page 4)
     (ALT-A) Enable MSR function
          -  enable or disable the magnetic card reader
     
     (ALT-B) Send start/stop code
          -  when this option is enable, it will send the start and stop
             code of each track 

     (ALT-C) Enable track 1 reader
          -  enable or disable the track 1 reader
     
     (ALT-D) Enable track 2 reader
          -  enable or disable the track 2 reader
     
     (ALT-E) Enable track 3 reader
          -  enable or disable the track 3 reader
     
     (ALT-F) Preamble character
          -  characters sent before MSR data

     (ALT-G) Postamble character
          -  characters sent after MSR data

     (ALT-H) Preamble of track 1
          -  characters sent before the track 1 data

     (ALT-I) Preamble of track 2
          -  characters sent before the track 2 data

     (ALT-J) Preamble of track 3
          -  characters sent before the track 3 data

     (ALT-K) Postamble of track 1
          -  characters sent after the track 1 data

     (ALT-L) Postamble of track 2
          -  characters sent after the track 2 data

     (ALT-M) Postamble of track 3
          -  characters sent after the track 3 data

     (ALT-N) MSR error beep
          -  when this option is enable, it will produce three audible beeps
             when the read code is error.


3.   Files On the Disk

     K78C.EXE       78 keys cherry switch programmable keyboard utility  version 5.10
     K78M.EXE       78 keys membrane programmable keyboard utility       version 5.10
     K120C.EXE      120 keys cherry switch programmable keyboard utility version 5.10
     K120M.EXE      120 keys membrane programmable keyboard utility      version 5.10
     K128C.EXE      128 keys cherry switch programmable keyboard utility version 5.10

     KBD-130.EXE    Barcode and MSR decoder utility version 1.30
note:
     KBD-130.EXE is for KBD100 EPROM version 1.3 and after.
     the new functions are
     1) KBD-130.EXE can be run under WIN95 DOS mode
     2) KBD-130.EXE can configure MSR preamble and postamble

     EGAVGA.BGI     Driver for EGA VGA monitor
     HERC.BGI       Driver for MONO monitor

     README.TXT     This file


4.   Keyboard Default Code

     78 key :
-----------------------------------------------------------------------------
1    |2    |3    |4    |5    |6    |7    |8    |9    |10   |11   |12   |13    
  `  |  1  |  2  |  3  |  4  |  5  |  6  |  7  |  8  |  9  |  0  |  F1 |  F2  
     |     |     |     |     |     |     |     |     |     |     |     |      
-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|----- 
14   |15   |16   |17   |18   |19   |20   |21   |22   |23   |24   |25   |26    
  Esc|  a  |  b  |  c  |  d  |  e  |  f  | Num | /   | *   | -   |  F3 |  F4  
     |     |     |     |     |     |     | Lock|     |     |     |     |      
-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|----- 
27   |28   |29   |30   |31   |32   |33   |34   |35   |36   |37   |38   |39    
  Tab|  g  |  h  |  i  |  j  |  k  |Back | 7   | 8   | 9   | +   |  F5 |  F6  
     |     |     |     |     |     |Space| Home| Up  | PgUp|     |     |      
-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|----- 
40   |41   |42   |43   |44   |45   |46   |47   |48   |49   |50   |51   |52    
 Ctrl|  l  |  m  |  n  |  o  |  p  | Caps| 4   | 5   | 6   |Print|  F7 |  F8  
 (L) |     |     |     |     |     | Lock| Left|     |Right|     |     |      
-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|----- 
53   |54   |55   |56   |57   |58   |59   |60   |61   |62   |63   |64   |65    
Shift|  q  |  r  |  s  |  t  |  u  | Scrl| 1   | 2   | 3   |Pause|  F9 |  F10 
 (L) |     |     |     |     |     | Lock| End | Down| PgDn|     |     |      
-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|----- 
66   |67   |68   |69   |70   |71   |72   |73   |74   |75   |76   |77   |78    
 Alt |  v  |  w  |  x  |  y  |  z  |   \ | 0   |Space| .   |Enter|  F11|  F12 
 (L) |     |     |     |     |     |     | Ins |     | Del | (L) |     |      
----------------------------------------------------------------------------- 



version 1.3
