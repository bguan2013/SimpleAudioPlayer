package group;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;


public class SwingAudioPlayer extends JFrame implements ActionListener 
{
	private final static int RECENT_FILE_QUANTITY = 5;

	private AudioPlayer player = new AudioPlayer();
	private Thread playbackThread;
	private PlayingTimer timer;

	private boolean isPlaying = false;
	private boolean isPause = false;
	private boolean useFile = false;
	private boolean usePlayList = false;
	private boolean useRandom = false;

	private int listIndex = 0;

	private String audioFilePath;
	private String audioFileName;
	private String lastOpenPath;
	protected ArrayList <String> audioPathNameList = new ArrayList();
	protected ArrayList <String> audioFileNameList = new ArrayList();

	protected Random r = new Random();

	protected File list = new File("group/list.txt");
	static protected BufferedWriter fw;
	protected FileReader flr;
	BufferedReader listReader;

	private JLabel labelFileName = new JLabel("Playing File:");
	private JLabel labelTimeCounter = new JLabel("00:00:00");
	private JLabel labelDuration = new JLabel("00:00:00");
	
	private JButton buttonPlay = new JButton();
	private JButton buttonStop = new JButton();
	private JButton buttonBack = new JButton();
	private JButton buttonNext = new JButton();
	private JToggleButton buttonRandom = new JToggleButton();
	private JButton buttonPlayList = new JButton();
	private JSlider sliderTime = new JSlider();

	private JMenuBar menubar = new JMenuBar();

	private JMenu file;
	private JMenuItem openFile;
	private JMenu openRecent;
	private String[][] recentFiles;
	private JMenuItem recent1;
	private JMenuItem recent2;
	private JMenuItem recent3;
	private JMenuItem recent4;
	private JMenuItem recent5;
	private JMenuItem themes; 

	private JMenu helpAndAbout;
	private JMenuItem help;
	private JMenuItem about;

	private JMenu aboutDisabled;

	private ImageIcon iconPlaylistTemp = new ImageIcon(getClass().getResource("icons/playlist.png"));
	Image imgTemp = iconPlaylistTemp.getImage().getScaledInstance(16,16,java.awt.Image.SCALE_SMOOTH);
	
	ImageIcon iconPlaylist = new ImageIcon(imgTemp);
	private ImageIcon iconRandom = new ImageIcon(getClass().getResource("icons/random.png"));
	private ImageIcon iconPlay = new ImageIcon(getClass().getResource("icons/play.png"));
	private ImageIcon iconStop = new ImageIcon(getClass().getResource("icons/stop.png"));
	private ImageIcon iconPause = new ImageIcon(getClass().getResource("icons/pause.png"));
	private ImageIcon iconFoward = new ImageIcon(getClass().getResource("icons/foward.png"));
	private ImageIcon iconBack = new ImageIcon(getClass().getResource("icons/back.png"));
	
	PlaylistWindow p = new PlaylistWindow(); 
	
	public SwingAudioPlayer() 
	{
		super("Minimalistic Music Player");
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;

		file = new JMenu("File");
		openFile = new JMenuItem("Open File");	
		recentFiles = readRecentFiles();
		openRecent = createSubMenu();
		themes = new JMenuItem("Themes"); 
		file.add(openFile);
		file.add(openRecent);
		file.add(themes);
		openFile.addActionListener(this);
		themes.addActionListener(this);

		helpAndAbout = new JMenu("H&A");
		help = new JMenuItem("Help");
		about = new JMenuItem("About");
		helpAndAbout.add(help);
		helpAndAbout.add(about);
		help.addActionListener(this);
		about.addActionListener(this);

		menubar.add(file);
		menubar.add(helpAndAbout);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(buttonRandom);
		menubar.add(buttonPlayList);
		
		setJMenuBar(menubar);

		buttonRandom.setIcon(iconRandom);
		buttonRandom.setEnabled(true);
		//buttonOpen.setFont(new Font("Sans", Font.BOLD, 14));
		//buttonOpen.setIcon(iconOpen);
		
		//buttonPlay.setFont(new Font("Sans", Font.BOLD, 14));
		buttonPlay.setIcon(iconPlay);
		buttonPlay.setEnabled(false);
		
		//buttonStop.setFont(new Font("Sans", Font.BOLD, 14));
		buttonStop.setIcon(iconStop);
		buttonStop.setEnabled(false);
		
		//buttonPlayList.setFont(new Font("Sans", Font.BOLD, 14));
		buttonPlayList.setIcon(iconPlaylist);

		buttonNext.setIcon(iconFoward);
		buttonNext.setEnabled(false);
		buttonBack.setIcon(iconBack);
		buttonBack.setEnabled(false);

		labelTimeCounter.setFont(new Font("Sans", Font.BOLD, 12));
		labelDuration.setFont(new Font("Sans", Font.BOLD, 12));
		
		sliderTime.setPreferredSize(new Dimension(400, 20));
		sliderTime.setEnabled(false);
		sliderTime.setPaintLabels(false);
		sliderTime.setValue(0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		add(labelFileName, constraints);
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		add(labelTimeCounter, constraints);
		
		constraints.gridx = 1;
		add(sliderTime, constraints);
		
		constraints.gridx = 2;
		add(labelDuration, constraints);
		
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

		panelButtons.add(buttonBack);
		panelButtons.add(buttonPlay);
		panelButtons.add(buttonNext);
		panelButtons.add(buttonStop);

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(panelButtons, constraints);
		
		//buttonOpen.addActionListener(this);
		buttonPlay.addActionListener(this);
		buttonStop.addActionListener(this);
		buttonPlayList.addActionListener(this);
		buttonNext.addActionListener(this);
		buttonBack.addActionListener(this);
		buttonRandom.addActionListener(this);

		pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);		
	}

	
	@Override
	public void actionPerformed(ActionEvent event) 
	{
		int pastIndex = listIndex;
		Object source = event.getSource();
		if(source instanceof JButton) 
		{
			JButton button = (JButton) source;
			
			if(button == buttonPlay) 
			{
				if (!isPlaying)
					if(audioPathNameList != null && usePlayList)
						playPlayList();
					else
						playBack();
				
				else if(isPause && isPlaying)
					resumePlaying();
				else
					pausePlaying();
			} 
			else if(button == buttonStop)
				stopPlaying();
			else if(button == buttonPlayList)
				openPlayList();
			else if(button == buttonNext)
			{
				if(useRandom)
					while(pastIndex == (listIndex = r.nextInt(audioFileNameList.size())))
					{	
						pastIndex = listIndex;
						listIndex = r.nextInt(audioFileNameList.size());
						System.out.println(pastIndex + " " + listIndex);
					}
				else
				{
					if(listIndex == audioPathNameList.size() - 1)
						listIndex = 0;
					else
						listIndex++;
				}

				//System.out.println(listIndex);

				resetControls();
				stopPlaying();
				playBack();
			}
			else if(button == buttonBack)
			{
				if(useRandom)
					while(pastIndex == (listIndex = r.nextInt(audioFileNameList.size())))
					{	
						pastIndex = listIndex;
						listIndex = r.nextInt(audioFileNameList.size());
						System.out.println(pastIndex + " " + listIndex);
					}
				else
				{
					if(listIndex == 0)
						listIndex = audioPathNameList.size() - 1;
					else
						listIndex--;
				}

				//System.out.println(listIndex);

				resetControls();
				stopPlaying();
				playBack();
			}
		}
		else if(source instanceof JToggleButton)
		{
			if(((JToggleButton) source).isSelected())
				useRandom = true;
			else
				useRandom = false;
		}
		else if(source instanceof JMenuItem){
			JMenuItem item = (JMenuItem)source;
			if(item == openFile){
				openFile();
				useFile = true;
				usePlayList = false;
			}
			else if(item == themes){

			}
			else if(item == about){

				menubar.removeAll();
				menubar.add(file);
				menubar.add(helpAndAbout);
				aboutDisabled = new JMenu("MMC Music Player V0.10");
				aboutDisabled.setEnabled(false);
				menubar.add(aboutDisabled);
				menubar.add(Box.createHorizontalGlue());
				menubar.add(buttonPlayList);
			}
			else if(item == help){
				buttonPlay.setText("Play");
				buttonStop.setText("Stop");
				buttonBack.setText("Previous");
				buttonNext.setText("Next");
				buttonPlayList.setText("Playlist");
			}
			else if(item == recent1){
				openFile(recentFiles[0][0], recentFiles[1][0]);
			}
			else if(item == recent2){
				openFile(recentFiles[0][1], recentFiles[1][1]);
			}
			else if(item == recent3){
				openFile(recentFiles[0][2], recentFiles[1][2]);
			}
			else if(item == recent4){
				openFile(recentFiles[0][3], recentFiles[1][3]);
			}
			else if(item == recent5){
				openFile(recentFiles[0][4], recentFiles[1][4]);
			}
		}
	}

	private void openFile() 
	{
		JFileChooser fileChooser = null;
		
		if (lastOpenPath != null && !lastOpenPath.equals(""))
			fileChooser = new JFileChooser(lastOpenPath);
		else
			fileChooser = new JFileChooser();
		
		FileFilter wavFilter = new FileFilter() 
		{
			@Override
			public String getDescription() {
				return "Sound file (*.WAV)";
			}

			@Override
			public boolean accept(File file) 
			{
				if (file.isDirectory())
					return true;
				else
					return file.getName().toLowerCase().endsWith(".wav");
			}
		};
		
		fileChooser.setFileFilter(wavFilter);
		fileChooser.setDialogTitle("Open Audio File");
		fileChooser.setAcceptAllFileFilterUsed(false);

		int userChoice = fileChooser.showOpenDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) 
		{
			audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			audioFileName = fileChooser.getSelectedFile().getName();
			lastOpenPath = fileChooser.getSelectedFile().getParent();
			
			try
			{
				writePlaylist(audioFilePath);
				syncPlaylist();
			}
			catch(IOException i)
			{
				System.out.println("Something happended");	
			}

			//These are updating the file Jmenu
			addToRecent(audioFileName, audioFilePath);
			file.removeAll();
			openRecent = createSubMenu();
			file.add(openFile);
			file.add(openRecent);
			file.add(themes);

			if (isPlaying || isPause) 
			{
				stopPlaying();
				while (player.getAudioClip().isRunning()) 
				{
					try 
					{
						Thread.sleep(100);
					}catch (InterruptedException ex) 
					{
						ex.printStackTrace();
					}
				}
			}
			playBack();
		}
	}


	/*
	This is used for openning recent file
	*/
	private void openFile(String name, String location) 
	{
		
			audioFilePath = location;
			audioFileName = name;
	
			if (isPlaying || isPause) 
			{
				stopPlaying();
				while (player.getAudioClip().isRunning()) 
				{
					try 
					{
						Thread.sleep(100);
					}catch (InterruptedException ex) 
					{
						ex.printStackTrace();
					}
				}
			}

		playBack();		
	}


	private void playBack() 
	{
		timer = new PlayingTimer(labelTimeCounter, sliderTime);
		timer.start();
		isPlaying = true;
		syncPlaylist();
		playbackThread = new Thread(new Runnable() 
		{

			@Override
			public void run() 
			{
				try 
				{
					//buttonPlay.setText("Pause");
					buttonPlay.setIcon(iconPause);
					buttonPlay.setEnabled(true);
					
					//buttonStop.setText("Stop");
					buttonStop.setEnabled(true);

					int pastIndex = listIndex;
					
					
					if(audioPathNameList != null && usePlayList)
					{	
						player.load(audioPathNameList.get(listIndex));
						labelFileName.setText("Playing File: " + p.audioFileNameList.get(listIndex));
					}
					else
					{
						player.load(audioFilePath);
						labelFileName.setText("Playing File: " + audioFileName);
					}

					timer.setAudioClip(player.getAudioClip());
					sliderTime.setMaximum((int) player.getClipSecondLength());
					
					labelDuration.setText(player.getClipLengthString());
					player.play();

					if(audioPathNameList == null || useFile)
						resetControls();
					else if(usePlayList && sliderTime.getMaximum() == sliderTime.getValue())
					{
						if(useRandom)
							while(pastIndex == (listIndex = r.nextInt(audioFileNameList.size())))
							{	
								pastIndex = listIndex;
								listIndex = r.nextInt(audioFileNameList.size());
								System.out.println(pastIndex + " " + listIndex);
							}
						else
						{
							if(listIndex == audioPathNameList.size() - 1)
								listIndex = 0;
							else
								listIndex++;
						}

						resetControls();
						stopPlaying();
						playBack();
					}
				}catch (UnsupportedAudioFileException ex) 
				{
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"The audio format is unsupported!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				} catch (LineUnavailableException ex) 
				{
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"Could not play the audio file because line is unavailable!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				} catch (IOException ex) 
				{
					JOptionPane.showMessageDialog(SwingAudioPlayer.this,  
							"I/O error while playing the audio file!", "Error", JOptionPane.ERROR_MESSAGE);
					resetControls();
					ex.printStackTrace();
				}

			}
		});

		playbackThread.start();

	}

	private void stopPlaying() 
	{
		isPause = false;
		isPlaying = false;
		//buttonStop.setText("Stop");
		buttonPlay.setIcon(iconPlay);
		buttonStop.setEnabled(false);
		timer.reset();
		timer.interrupt();
		player.stop();
		playbackThread.interrupt();
	}
	
	private void pausePlaying() 
	{
		//buttonPlay.setText("Resume");
		buttonPlay.setIcon(iconPlay);
		isPause = true;
		player.pause();
		timer.pauseTimer();
		playbackThread.interrupt();
	}
	
	private void resumePlaying() 
	{
		//buttonPlay.setText("Pause");
		buttonPlay.setIcon(iconPause);
		isPause = false;
		player.resume();
		timer.resumeTimer();
		playbackThread.interrupt();		
	}
	
	private void resetControls() 
	{
		timer.reset();
		timer.interrupt();

		//buttonPlay.setText("Play");
		buttonPlay.setIcon(iconPlay);
		
		buttonStop.setEnabled(false);
		
		isPlaying = false;		
	}

	private void openPlayList()
	{
		syncPlaylist();
		
		if(!p.isShowing())
		{
			useFile = false;
			usePlayList = true;
			p.setVisible(true);
			//System.out.println(audioFileNameList.size());
			if(useRandom)
				listIndex = r.nextInt(audioFileNameList.size());
			else
				listIndex = 0;
			//System.out.println(listIndex);
			buttonPlay.setEnabled(true);
		}
		else
			System.out.println("Already showing the playlist window idiot.");
	}	

	private void playPlayList()
	{
		buttonNext.setEnabled(true);
		buttonBack.setEnabled(true);
		//syncPlaylist();
		playBack();
	}

	/*
	This is for reading the saved file names
	Also the array should be a 2D array
	First array for the names of the songs 
	And second array for the paths of the songs
	*/
	private String[][] readRecentFiles(){
		
		String[][] temp = new String[2][RECENT_FILE_QUANTITY];
		File file;
		FileReader fr;
		BufferedReader br;
		try{
			file = new File("recent.dat");

			if(!file.exists()){
				file.createNewFile();
				
			}
			else{
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				
				String tempNames = br.readLine();
				if(tempNames != null){
				String tempPaths = br.readLine();
				String[] tempArray1 = tempNames.substring(3).split("#!#");
				String[] tempArray2 = tempPaths.substring(3).split("#!#");
				for(int i = 0; i < tempArray1.length; i++){
					temp[0][i] = tempArray1[i];
				}
				for (int i = 0; i < tempArray2.length; i++) {
					temp[1][i] = tempArray2[i];
				}

				}			
 			
				br.close();
				fr.close();
			}
		}
		catch(IOException e){

			e.printStackTrace();
		}
		
		return temp;		
	}

	/*
	MUST pass in the file NAME and the path
	*/
	private void addToRecent(String name, String location){

		if(recentFiles[0][RECENT_FILE_QUANTITY-1] != null){
			for(int i = 1; i < RECENT_FILE_QUANTITY; i++){
				recentFiles[0][i-1] = recentFiles[0][i];
				recentFiles[1][i-1] = recentFiles[1][i];
			}
			recentFiles[0][RECENT_FILE_QUANTITY-1] = name;
			recentFiles[1][RECENT_FILE_QUANTITY-1] = location;
		}
		else{
			for(int i = 0; i < RECENT_FILE_QUANTITY; i++){
				if(recentFiles[0][i] == null){
					recentFiles[0][i] = name;
					recentFiles[1][i] = location;
					return;
				}
			}
		}
	}

	private JMenu createSubMenu()
	{
		JMenu temp = new JMenu("Recent");
		for(int i = 0; i < RECENT_FILE_QUANTITY; i++){
			if(recentFiles[0][i] != null){

				if(i == 0){
					recent1 = new JMenuItem(recentFiles[0][i]);
					recent1.addActionListener(this);
					temp.add(recent1);
				}
				else if(i == 1){
					recent2 = new JMenuItem(recentFiles[0][i]);
					recent2.addActionListener(this);
					temp.add(recent2);

				}
				else if(i == 2){
					recent3 = new JMenuItem(recentFiles[0][i]);
					recent3.addActionListener(this);
					temp.add(recent3);
				}
				else if(i == 3){
					recent4 = new JMenuItem(recentFiles[0][i]);
					recent4.addActionListener(this);
					temp.add(recent4);
				}
				else if(i == 4){
					recent5 = new JMenuItem(recentFiles[0][i]);
					recent5.addActionListener(this);
					temp.add(recent5);
				}
				
			}

		}

		return temp;
	}
	
	//Name of the songs can't contain the string sequence #!#
	//This is the splitters 

	private void saveRecentFiles(){

		try{

			String tempNames = "";
			String tempPaths = "";

			for(int i = 0; i < RECENT_FILE_QUANTITY; i++){
				if(recentFiles[0][i] != null){
					tempNames = tempNames + "#!#" + recentFiles[0][i];
					tempPaths = tempPaths + "#!#" + recentFiles[1][i];
				}
			}
			String tempSum = tempNames + "\n" + tempPaths;
			File recentFileName = new File("recent.dat");
			FileWriter fw = new FileWriter(recentFileName);

			if(!tempNames.equals("")){
				fw.write(tempSum);
			}
			fw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void attachHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					saveRecentFiles();
				}
		});
	}

	public static void createGUI(){
		SwingAudioPlayer sap = new SwingAudioPlayer();
		sap.attachHook();
	}

	public void syncPlaylist()
	{
		useFile = false;
		usePlayList = true;

		buttonNext.setEnabled(true);
		buttonBack.setEnabled(true);	
		try
		{
			loadPlaylist();
		}
		catch(IOException i)
		{}
		p.setSongList();
		p.audioFileNameList = audioFileNameList;
		p.audioPathNameList = audioPathNameList;
	}

	public void writePlaylist(String file) throws IOException
	{
		if(fw == null)
			fw = new BufferedWriter(new FileWriter(list, true));
		//System.out.println(file);

		fw.append(file);
		fw.append("\r\n");
		fw.flush();
	}

	public void loadPlaylist()throws IOException
	{
		String s;
		Scanner in = new Scanner(list);

		if(audioPathNameList != null)
        {
            audioPathNameList.clear();
            audioFileNameList.clear();
        }

		while(in.hasNext())
		{
			s = in.nextLine();
			//System.out.println(s);
			audioPathNameList.add(s);
			System.out.println(s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.')));
			audioFileNameList.add(s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.')));
		}
		in.close();
	}

	public static void main(String[] args)throws IOException 
	{

		try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
			UIManager.put("Slider.paintValue", false);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SwingAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SwingAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SwingAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SwingAudioPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createGUI();				
			}
		});
	}
}
