import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    private String data;
    private long timeStamp;
    private int foo;

    public  Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculated_hash = StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(foo) + data);
        return calculated_hash;
    }

    public  void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(target)) {
            foo++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!! : " + hash);
    }

}
