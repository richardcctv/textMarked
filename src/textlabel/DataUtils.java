package textlabel;

import java.io.*;
import java.util.*;

public class DataUtils {
    private static String dataDir = null;
    private static String configPath= null;
    private static  UI ui = new UI();
    public static String FINISH_INFO = "文件均已标记过";
    //数据信息相关
    private static List<String> dataFiles = new ArrayList<>();//数据目录下的文件
    private static String typeDir;//不同类型的文本写入目录，根据配置文件获取
    private static Map<String,int[]> gmap =  new HashMap<>();//群信息获取
    private static Map<String,int[]> zmap = new HashMap<>();//账号信息获取
    private static String gFile ;//gmap对应的目录
    private static  String wfile ;//wmap对应的目录
    private static int GID_INDEX = 0;
    private static int WID_INDEX =0;
    private static int CONTENT_INDEX = 0;
    private static String cur_file_name ;
    //写入工具
    private static BufferedWriter[] writers = new BufferedWriter[TypeEnum.values().length];

    //缓存链表相关
    private static int buffer_size = 0;
    private static BufferContextNode newest;
    private static BufferContextNode oldest;
    private static BufferContextNode cur;

    private static BufferedReader curReader ;//当前文件读工具
    private static int cur_index = 0;//当前文件下标

    public static void setConfigPath(String config) {
        configPath = config;
    }

    public static void setDataPath(String dataDir) {
        DataUtils.dataDir = dataDir;
    }

    public static boolean getHasConfigPath() {
        return configPath!=null;
    }

    public static boolean getHasDataPath() {
        return dataDir!=null;
    }

    public static void init(){

        //根据配置文件在信息和数据信息来初始化DataUtils的字段
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(new File(configPath)));
        } catch (IOException e){
            System.out.println("加载配置文件出错...");
        }

        gFile = properties.getProperty("GID_PATH");
        wfile = properties.getProperty("WID_PATH");
        //初始化map
        initMap(gmap,gFile);
        initMap(zmap,wfile);

        typeDir = properties.getProperty("TYPE_DIR");
        WID_INDEX = Integer.parseInt(properties.getProperty("WID_INDEX"));
        GID_INDEX = Integer.parseInt(properties.getProperty("GID_INDEX"));
        CONTENT_INDEX = Integer.parseInt(properties.getProperty("CONTENT_INDEX"));

        //写入文件工具初始化
        try{
            for(int i = 0;i<TypeEnum.values().length;i++) {
                String fileName = typeDir+File.separator+(TypeEnum.values()[i]);
                writers[i] = new BufferedWriter(new FileWriter(fileName,true));//写入时追加
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }

        File dir = new File(dataDir);
        for(String s : dir.list()){
            if("tmp".equals(s)){
                //如果发现了"tmp"文件需要优先处理
                try {
                    cur_file_name = "tmp";
                    curReader = new BufferedReader(new FileReader(new File(dataDir + "tmp")));
                }catch (IOException e){
                    System.out.println("init curReader from tmp error");
                }
            }
            if(new File(dataDir+s).isFile()){
                dataFiles.add(dataDir+s);
            }
        }
        if(curReader==null){
            //根据文件名选择一个最新的或者上次没有读过的文件，然后给curReader赋值
            getNewReader();
        }
        if( curReader!=null ){
            buffer_size = Integer.parseInt(properties.getProperty("BUFFER_SIZE"));
            initBufferList();
            //获取buffer内容给ui
            if(cur.getContent()!=null)
                ui.getText(cur.getContent());
            else ui.getText(FINISH_INFO);
        } else {
                 ui.getText(FINISH_INFO);
        }
    }
    private static void initMap(Map<String,int[]> map,String fn){
        BufferedReader br = null;
        try {
            String line = null;
            File file = new File(fn);
            if(!file.exists())
                file.createNewFile();
            br = new BufferedReader(new FileReader(file));
            while((line=br.readLine())!=null){
                String[] info = line.split("\t");
                String key = info[0];
                int[] counters = new int[TypeEnum.values().length];
                for(int i = 0;i<counters.length;i++){
                    counters[i] = Integer.parseInt(info[i+1]);
                }
                map.put(key,counters);
            }
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("读map文件出错...");
        }
    }
    private static void getNewReader(){
        //当前reader先关闭掉
        try{
            if(curReader!=null){
                curReader.close();
                //如果是tmp文件，则不做处理.其他文件名，修改下名字
                if(!"tmp".equals(cur_file_name)){
                    File now = new File(dataDir+cur_file_name);
                    File modified = new File(dataDir+"finished-"+cur_file_name);
                    now.renameTo(modified);
                }
            }
        }catch (IOException e){
            System.out.println("关闭当前reader出错...");
        }
        //首先来看有没有缺少"finished-"前缀的文件（新文件）
        String target = null;
        for(int i = 0;i<dataFiles.size();i++){
            String fname = dataFiles.get(i);
            System.out.println(fname);
            String fn = fname.substring(fname.lastIndexOf(File.separator)+1);
            if(!fn.startsWith("finished-")){
                target = fname;
                cur_index = i;
                cur_file_name = fn;
                break;
            }
        }
        if (target==null){
            curReader = null;
        } else {
            try {
                curReader = new BufferedReader(new FileReader(target));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void initBufferList(){
        oldest = new BufferContextNode();
        fillBufferContextNode(oldest,getNextLine());
        cur = oldest;
        for( int i = 1; i<buffer_size; i++){
            cur.setNext(new BufferContextNode());
            cur.getNext().setLast(cur);
            cur = cur.getNext();
            fillBufferContextNode(cur,getNextLine());
        }
        newest = cur;
        cur = oldest;
    }
    private static void fillBufferContextNode(BufferContextNode node,String line){
        if(line!=null){
            String[] info = line.split("\t");
            node.setGid(info[GID_INDEX]);
            node.setWid(info[WID_INDEX]);
            node.setContent(info[CONTENT_INDEX]);
        } else {
            //假如没有需要标记的数据了，将这个节点清空
            node.clearContent();
        }
    }
    private static String getNextLine(){
        String line = null;
        try{
            if(curReader==null){
               // return null;
            } else if ((line=curReader.readLine())!=null) {
                // return line;
            } else {
                getNewReader();
                if(curReader!=null){
                    line = curReader.readLine();
                }
            }
        } catch (IOException e){
                System.out.println("curReader read error...");
        }
        return line;
    }
    public static void mark(String symbol){
        System.out.println(symbol);
        TypeEnum te = TypeEnum.valueOf(symbol);
        //1.更新当前的buffer数据
        cur.setType(te);
        //2.更新当前在数据
        cur = cur.getNext();
        if(cur==null){//buffer到达最新的数据了，将旧数据写入文件并更新数据
            save();
            //3.UI数据更新
            readMore();
        }
        if(cur!=null)
            ui.getText(cur.getContent());
    }
    private static void save(){
        flushMap(gmap,oldest.getGid(),oldest);
        flushMap(zmap,oldest.getWid(),oldest);

        BufferedWriter bw = writers[oldest.getType().ordinal()];
        try{
            bw.write(oldest.getContent()+"\n");
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("写入文本信息出错");
        }
    }

    //将数据更新到map里
    public static void flushMap(Map<String,int[]> map,String key,BufferContextNode bcn){
        System.out.println(bcn.getType());
        int[] counters = map.getOrDefault(key,null);
        if(counters==null)
            counters = new int[TypeEnum.values().length];
        counters[bcn.getType().ordinal()]++;
        map.put(key,counters);

    }

    //读一条新的数据到oldest里，然后将oldest这条数据放到最新的位置
    private static void readMore(){
        String newLine = getNextLine();
        if(newLine!=null){
            fillBufferContextNode(oldest,newLine);
            oldest.setLast(newest);
            newest.setNext(oldest);
            oldest = oldest.getNext();
            oldest.setLast(null);
            newest.getNext().setNext(null);
            newest = newest.getNext();
            cur = newest;
        }
    }
    //UI离开前的保存工作
    public static void finish(){
        //保存map文件
        saveMap(gmap,gFile);
        saveMap(zmap,wfile);

        try{
            if(curReader!=null){
                copyUnfinished();
            }
            //当前文件关闭
            for(BufferedWriter b : writers){
                b.flush();
                b.close();
            }
        } catch (IOException e) {
            System.out.println("离开前文件关闭出错...");
        }

        //文件重命名
        if(!"tmp".equals(cur_file_name)){
            File now = new File(dataFiles.get(cur_index));
            File update = new File(dataDir+"finished-"+now.getName());
            now.renameTo(update);
        }
    }

    private static void saveMap(Map<String,int[]> map,String fName){
        BufferedWriter bw  = null;
        StringBuilder builder = new StringBuilder();
        try {
            bw = new BufferedWriter(new FileWriter(fName));
            for(String key : map.keySet()){
                builder.append(key);
                int[] counters = map.get(key);
                for(int counter : counters){
                    builder.append("\t"+counter);
                }
                builder.append("\n");
                bw.write(builder.toString());
                builder.delete(0,builder.length());
            }
            bw.flush();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(bw!=null){
                try{
                    bw.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
             }
        }
    }
    private static void copyUnfinished(){
        BufferedWriter bw =null;
        try{
            String line = curReader.readLine();
            if(line!=null){
                bw = new BufferedWriter(new FileWriter(dataDir+"tmp-001"));
                bw.write(line+"\n");
                while((line=curReader.readLine())!=null){
                    bw.write(line+"\n");
                }
                bw.flush();
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(bw!=null){
                try{
                    bw.close();
                    curReader.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        //删除原来的tmp文件
        if("tmp".equals(cur_file_name))
            new File(dataDir+"tmp").delete();

        File now = new File(dataDir+"tmp-001");
        File modify = new File(dataDir+"tmp");
        now.renameTo(modify);
    }
    public  static void show(){
        ui.init();
    }
}
