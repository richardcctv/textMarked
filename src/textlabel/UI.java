package textlabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UI {
    private JFrame jFrame = new JFrame("文本标准工具");
        //part one
    private Button selProFile = new Button("config");
    private Button selDataFile = new Button("data");
    private Panel pone = new Panel();

        //part two
    private TextArea textArea = new TextArea(30,100);
    private Panel bpanel = new Panel();
    private Button[] textTypes;
    private Button last = new Button("LAST");
    public UI(){
        textTypes = new Button[TypeEnum.values().length];
        for(int i = 0;i<textTypes.length;i++){
            textTypes[i] = new Button(TypeEnum.values()[i].name());
        }
    }

    public void init(){
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataUtils.finish();
                System.exit(0);
            }
        });
        selProFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog(jFrame);
                fileDialog.setVisible(true);
                DataUtils.setConfigPath(fileDialog.getDirectory()+fileDialog.getFile());
                if(DataUtils.getHasConfigPath()&&DataUtils.getHasDataPath()){
                    reShow();
                }
            }
        });

        selDataFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog(jFrame);
                fileDialog.setVisible(true);
                DataUtils.setDataPath(fileDialog.getDirectory());
                if(DataUtils.getHasConfigPath()&&DataUtils.getHasDataPath()){
                    reShow();
                }
            }
        });
        pone.setBounds(0,0,30,100);
        pone.setLayout(new FlowLayout());
        pone.add(selProFile);
        pone.add(selDataFile);
//        jFrame.setLayout(new FlowLayout());
//        jFrame.add(selDataFile);
//        jFrame.add(selProFile);
        jFrame.add(pone);
        jFrame.pack();
        jFrame.setVisible(true);
    }
    public void reShow(){

        pone.setVisible(false);
        selDataFile.setVisible(false);
        selProFile.setVisible(false);

        DataUtils.init();
        jFrame.setLayout(new BorderLayout());
        jFrame.add(textArea,BorderLayout.CENTER);

        bpanel.setLayout(new GridLayout(textTypes.length+1,1));
        for(Button b : textTypes){
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!DataUtils.FINISH_INFO.equals(textArea.getText()))
                        DataUtils.mark(b.getLabel()
                    );
                }
            });
            bpanel.add(b);
        }
        bpanel.add(last);

        jFrame.add(bpanel,BorderLayout.EAST);
        jFrame.pack();
    }
    public void getText(String s){
        StringBuilder builder = new StringBuilder(s);
        int i = 1;
        while(s.length()>i*textArea.getColumns()){
            builder.insert(i*textArea.getColumns()+i-1,"/n");
            i++;
        }
        textArea.setText(builder.toString());
    }
}
