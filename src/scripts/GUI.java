package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.JLabel;
import org.tribot.util.Util;

public class GUI extends javax.swing.JFrame 
{
	private static final long serialVersionUID = 1L;
	
	public GUI() 
    {
        initComponents();
        loadSettings();
    }

    private void initComponents()
    {
    	setUpPaths();
    	
        m_title = new javax.swing.JLabel();
        m_abLogLevelLabel = new javax.swing.JLabel();
        m_abLogLevelSlider = new javax.swing.JSlider();
        m_abLevelLabel = new javax.swing.JLabel();
        m_abLevelSlider = new javax.swing.JSlider();
        m_runLabel = new javax.swing.JLabel();
        m_runSlider = new javax.swing.JSlider();
        m_saveProgsCheckBox = new javax.swing.JCheckBox();
        m_runButton = new javax.swing.JButton();

        setAlwaysOnTop(true);
        setMaximumSize(new java.awt.Dimension(711, 405));
        setMinimumSize(new java.awt.Dimension(711, 405));
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);
        getContentPane().setLayout(new java.awt.GridLayout(9, 0));

        m_title.setFont(new java.awt.Font("Futura Md BT", 0, 22)); // NOI18N
        m_title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_title.setText("INFINITE BANANA by BOTSLAVE");
        getContentPane().add(m_title);

        m_abLogLevelLabel.setFont(new java.awt.Font("Futura Md BT", 0, 14)); // NOI18N
        m_abLogLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_abLogLevelLabel.setLabelFor(m_abLogLevelSlider);
        m_abLogLevelLabel.setText("ANTIBAN LOG VERBOSITY");
        getContentPane().add(m_abLogLevelLabel);

        m_abLogLevelSlider.setFont(new java.awt.Font("Futura Md BT", 0, 11)); // NOI18N
        m_abLogLevelSlider.setMajorTickSpacing(1);
        m_abLogLevelSlider.setMaximum(2);
        m_abLogLevelSlider.setPaintLabels(true);
        m_abLogLevelSlider.setPaintTicks(true);
        m_abLogLevelSlider.setSnapToTicks(true);
        m_abLogLevelSlider.setValue(1);
        Hashtable<Integer, JLabel> labelsABLLS = new Hashtable<Integer, JLabel>();
        labelsABLLS.put(0, new JLabel("None"));
        labelsABLLS.put(1, new JLabel("Low"));
        labelsABLLS.put(2, new JLabel("High"));
        m_abLogLevelSlider.setLabelTable(labelsABLLS);
        getContentPane().add(m_abLogLevelSlider);

        m_abLevelLabel.setFont(new java.awt.Font("Futura Md BT", 0, 14)); // NOI18N
        m_abLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_abLevelLabel.setLabelFor(m_abLevelSlider);
        m_abLevelLabel.setText("ANTIBAN LEVEL");
        getContentPane().add(m_abLevelLabel);

        m_abLevelSlider.setFont(new java.awt.Font("Futura Md BT", 0, 11)); // NOI18N
        m_abLevelSlider.setMajorTickSpacing(50);
        m_abLevelSlider.setMinorTickSpacing(5);
        m_abLevelSlider.setPaintLabels(true);
        m_abLevelSlider.setPaintTicks(true);
        m_abLevelSlider.setSnapToTicks(true);
        m_abLevelSlider.setValue(10);
        Hashtable<Integer, JLabel> labelsABLL = new Hashtable<Integer, JLabel>();
        labelsABLL.put(0, new JLabel("None"));
        labelsABLL.put(50, new JLabel("High"));
        labelsABLL.put(100, new JLabel("Ultra"));
        m_abLevelSlider.setLabelTable(labelsABLL);
        getContentPane().add(m_abLevelSlider);

        m_runLabel.setFont(new java.awt.Font("Futura Md BT", 0, 14)); // NOI18N
        m_runLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_runLabel.setLabelFor(m_runSlider);
        m_runLabel.setText("RUN THRESHHOLD");
        getContentPane().add(m_runLabel);

        m_runSlider.setFont(new java.awt.Font("Futura Md BT", 0, 11)); // NOI18N
        m_runSlider.setMinorTickSpacing(25);
        m_runSlider.setPaintLabels(true);
        m_runSlider.setPaintTicks(true);
        m_runSlider.setValue(80);
        Hashtable<Integer, JLabel> labelsRL = new Hashtable<Integer, JLabel>();
        labelsRL.put(0, new JLabel("0%"));
        labelsRL.put(50, new JLabel("50%"));
        labelsRL.put(100, new JLabel("100%"));
        m_runSlider.setLabelTable(labelsRL);
        getContentPane().add(m_runSlider);

        m_saveProgsCheckBox.setFont(new java.awt.Font("Futura Md BT", 0, 14)); // NOI18N
        m_saveProgsCheckBox.setText("SAVE PROGRESS SCREENSHOTS");
        m_saveProgsCheckBox.setFocusable(false);
        m_saveProgsCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_saveProgsCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        getContentPane().add(m_saveProgsCheckBox);

        m_runButton.setFont(new java.awt.Font("Futura Md BT", 0, 14)); // NOI18N
        m_runButton.setText("RUN");
        m_runButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                m_runButtonActionPerformed(evt);
            }
        });
        getContentPane().add(m_runButton);

        pack();
    }

	private void setUpPaths()
	{
		m_folder = new File(Util.getWorkingDirectory() + "\\InfiniteBananaSettings\\");
    	m_fullPath = new File(m_folder, SUPER.SCRIPT_NAME + "_settings.ini");
	}                  

    private void m_runButtonActionPerformed(java.awt.event.ActionEvent evt)
    {                                            
        SUPER.ANTIBAN_VERBOSITY = SUPER.LOG_VERBOSITY.values()[m_abLogLevelSlider.getValue()];
        SUPER.ANTIBAN_LEVEL = (double)m_abLevelSlider.getValue() / 100;
        SUPER.RUN_THRESHHOLD = m_runSlider.getValue();
        SUPER.SAVE_PROGS = m_saveProgsCheckBox.isSelected();
        
        saveSettings();
        
        SUPER.GUI_FINISHED = true;
    }                  
    
    private void saveSettings ()
    {
	    try 
	    {
	    	m_properties.clear();
	    	m_properties.put("ablls", String.valueOf(m_abLogLevelSlider.getValue()));   
	    	m_properties.put("abls", String.valueOf(m_abLevelSlider.getValue()));   
	    	m_properties.put("rs", String.valueOf(m_runSlider.getValue()));   
	    	m_properties.put("spc", String.valueOf(m_saveProgsCheckBox.isSelected()));   
	    	
	    	if (!m_folder.exists()) m_folder.mkdirs();
	    	
	    	m_properties.store(new FileOutputStream(m_fullPath), "Infinite Banana Settings");
	    } 
	    catch (Exception e1)
	    {
	        System.out.print("Unable to save settings");
	        e1.printStackTrace();
	    }
    }
    
    public void loadSettings()
    {   //we will call this externally    
        try
        {      
            if (!m_fullPath.exists())
            {             
            	m_fullPath.createNewFile();      //or make a new one        
            }      
            
            m_properties.load(new FileInputStream(m_fullPath));    
            
            m_abLogLevelSlider.setValue(Integer.parseInt(m_properties.getProperty("ablls")));
            
            m_abLevelSlider.setValue(Integer.parseInt(m_properties.getProperty("abls")));
            
            m_runSlider.setValue(Integer.parseInt(m_properties.getProperty("rs")));
            
            m_saveProgsCheckBox.setSelected(Boolean.parseBoolean(m_properties.getProperty("spc")));
        }
        catch (Exception e)
        {      
            System.out.print("Unable to load settings");      
            e.printStackTrace();   
        }
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel m_abLevelLabel;
    private javax.swing.JSlider m_abLevelSlider;
    private javax.swing.JLabel m_abLogLevelLabel;
    private javax.swing.JSlider m_abLogLevelSlider;
    private javax.swing.JCheckBox m_saveProgsCheckBox;
    private javax.swing.JButton m_runButton;
    private javax.swing.JLabel m_runLabel;
    private javax.swing.JSlider m_runSlider;
    private javax.swing.JLabel m_title;
    // End of variables declaration         
    
    // PROPERTIES
    public static Properties m_properties = new Properties();
    public static File m_folder;
    public static File m_fullPath;
}
