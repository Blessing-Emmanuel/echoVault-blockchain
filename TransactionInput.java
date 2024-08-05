public class TransactionInput {
    public String transactionOutputID;
    public TransactionOutput UTXO; // unspent transaction outputs

    public TransactionInput(String transactionOutputID) {
        this.transactionOutputID = transactionOutputID;
    }
}
