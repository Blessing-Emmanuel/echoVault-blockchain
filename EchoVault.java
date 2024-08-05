import java.security.Security;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class EchoVault {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        // Setup Bouncey Castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //Create genesis transaction , which sends 100 EchoVault to walletA
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionID = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receipient, genesisTransaction.value, genesisTransaction.transactionID));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis Block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWallet A's balance is: " + walletA.getBalance());
        System.out.println("\nWallet A is attempting to send funds (40) to Wallet B...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWallet A's balance is " + walletA.getBalance());
        System.out.println("\nWallet B's balance is " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWallet A is attempting to send more funds (1000) than it has... ");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWallet A's balance is " + walletA.getBalance());
        System.out.println("\nWallet B's balance is " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWallet B is attempting to send funds (20) to Wallet A... ");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20f));
        System.out.println("\nWallet A's balance is " + walletA.getBalance());
        System.out.println("\nWallet B's balance is " + walletB.getBalance());

        isChainValid();
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for(int i=1; i< blockchain.size(); i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            // compare registered hash and calculated hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("#Current Hashes not equal");
                return false;
            }

            // compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("#Previous Hashes not equal");
                return false;
            }

            // check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop through blockchains transactions
            TransactionOutput tempOutput;
            for (int t=0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on transaction(" + t +") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are not equal to outputs on transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputID);

                    if (tempOutput == null) {
                        System.out.println("#Referencedd input on transaction(" + t +") is Missing");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input transaction(" + t +") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputID);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).receipient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t +") output recipient is not who it should be");
                    return false;
                }

                if (currentTransaction.outputs.get(1).receipient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t +") output 'change' is not sender.");
                    return false;
                }
            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
