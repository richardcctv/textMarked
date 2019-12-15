package textlabel;

public class BufferContextNode {
    private String gid  = null;
    private String wid  = null;
    private String content = null;
    private TypeEnum type = null;
    private BufferContextNode next = null;
    private BufferContextNode last = null;

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public BufferContextNode getNext() {
        return next;
    }

    public void setNext(BufferContextNode next) {
        this.next = next;
    }

    public BufferContextNode getLast() {
        return last;
    }

    public void setLast(BufferContextNode last) {
        this.last = last;
    }

    public void clearContent(){
        this.setContent(null);
        this.setWid(null);
        this.setGid(null);
    }
}
