package group;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;

public class PlaylistWindow extends JFrame implements ActionListener
{
    private String audioFilePath;
    private String audioFileName;
    private String lastOpenPath;
    ArrayList <String> audioFileNameList = new ArrayList();
    ArrayList <String> audioPathNameList = new ArrayList();
    protected File list = new File("group/list.txt");
    static protected BufferedWriter fw;
    protected FileReader flr;
    BufferedReader listReader;
    private JButton addButton;
    private JButton delButton;
    private JScrollPane scrollSongList;
    private JTextArea SongList;

    public PlaylistWindow() 
    {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    
    private void initComponents() 
    {
        
        scrollSongList = new JScrollPane();
        SongList = new JTextArea();
        addButton = new JButton();
        delButton = new JButton();

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setPreferredSize(new Dimension(400, 400));

        scrollSongList.setEnabled(true);

        SongList.setColumns(20);
        SongList.setRows(5);
        SongList.setEditable(false);
        scrollSongList.setViewportView(SongList);

        addButton.setText("Add");
        addButton.addActionListener(this);
        delButton.setText("Del");
        delButton.addActionListener(this);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(scrollSongList, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(delButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrollSongList, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(delButton)))
        );

        pack();
        try
        {
            loadPlaylist();
            setSongList();
        }   
        catch(IOException i)
        {}
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Object source = event.getSource();
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            if (button == addButton) 
            {
                openFile();
            }
            else if (button == delButton) 
            {
                String fileName = JOptionPane.showInputDialog("Which song do you want to remove?");

                deleteFile(fileName);
            } 
        }
    }

    private void openFile() 
    {
        JFileChooser fileChooser = null;
        
        if (lastOpenPath != null && !lastOpenPath.equals("")) {
            fileChooser = new JFileChooser(lastOpenPath);
        } else {
            fileChooser = new JFileChooser();
        }
        
        FileFilter wavFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Sound file (*.WAV)";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return file.getName().toLowerCase().endsWith(".wav");
                }
            }
        };

        
        fileChooser.setFileFilter(wavFilter);
        fileChooser.setDialogTitle("Open Audio File");
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userChoice = fileChooser.showOpenDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) 
        {
            
            audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            //audioPathNameList.add(audioFilePath);
            audioFileName = fileChooser.getSelectedFile().getName();
            //audioFileNameList.add(audioFileName);
            lastOpenPath = fileChooser.getSelectedFile().getParent();
            try
            {
                writePlaylist(audioFilePath);
                loadPlaylist();

                int index = 0;
                SongList.setText("");

                for(String s : audioFileNameList)
                    SongList.append(++index + ". " + s + "\n");
            }
            catch(IOException i)
            {
                System.out.println("Something happended");  
            }
        }
    }

    private void deleteFile(String fileName)
    {
        for(int i = 0; i < audioFileNameList.size(); i++)
            if((fileName + ".wav").equals(audioFileNameList.get(i)))
                {
                    audioFileNameList.remove(i);
                    audioPathNameList.remove(i);
                }

        SongList.setText("");

        for(int i = 0; i < audioFileNameList.size(); i++)
            SongList.append((i + 1) + ". " + audioFileNameList.get(i) + "\n");

    }

    public void writePlaylist(String file) throws IOException
    {
        if(fw == null)
            fw = new BufferedWriter(new FileWriter(list, true));
        //System.out.println(file);

        fw.append(file);
        fw.append("\r\n");
        fw.flush();
        fw.close();
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
            //System.out.println(s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.')));
            audioFileNameList.add(s.substring(s.lastIndexOf('/') + 1, s.lastIndexOf('.')));
        }
        in.close();
    }

    public void setSongList()
    {
        int index = 0;

        SongList.setText("");
        for(String s : audioFileNameList)
            SongList.append(++index + ". " + s + "\n");
    }

    public static void main(String args[]) 
    {
        
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PlaylistWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PlaylistWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PlaylistWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PlaylistWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PlaylistWindow().setVisible(true);
            }
        });
    }
}
