import java.security.PublicKey;
import java.util.Calendar;

public class Loans {
    public String loanID;
    public PublicKey lender;
    public PublicKey borrower;
    public float amount;
    public float interestRate;
    public Calendar startDate;
    public Calendar endDate;
    public boolean isRePaid;

    public Loans(PublicKey lender, PublicKey borrower, float amount, float interestRate, int durationDays){
        this.loanID = StringUtil.applySha256(StringUtil.getStringFromKey(lender) +
                StringUtil.getStringFromKey(borrower) + amount + interestRate + durationDays +
                Calendar.getInstance().getTimeInMillis());
        this.lender = lender;
        this.borrower = borrower;
        this.amount = amount;
        this.interestRate = interestRate;
        this.startDate = Calendar.getInstance();
        this.endDate = Calendar.getInstance();
        this.endDate.add(Calendar.DAY_OF_YEAR, durationDays);
        this.isRePaid = false;
    }

    public void repayLoan() {
        this.isRePaid = true;
    }

    public String isDue() { 
        return "Loan: " + loanID + "is due: " + Calendar.getInstance().after(this.endDate);
    }
}
