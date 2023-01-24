package main.com.java.writer;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import main.com.java.model.Account;

@Component
public class AccountWriter implements ItemWriter<Account> {
	
	private static final String insertQuery = "INSERT INTO ACCOUNT VALUES (?,?,?,?,?,?)";
	private static final String checkQuery = "SELECT COUNT(*) FROM ACCOUNT WHERE ACCOUNT_ID = ?";
	private static final String updateQuery = "UPDATE ACCOUNT "
			+ "SET FIRST_NAME = ?, LAST_NAME = ?, MIDDLE_INITAL = ?, JOB = ?, BALANCE = ?"
			+ "WHERE ACCOUNT_ID = ?";
	
	@Autowired
	private JdbcTemplate template;
	
	@Override
	public void write(List<? extends Account> items) throws Exception {
		for(Account account : items) {
			int exists = template.queryForObject(checkQuery, new Object[] {account.getAccountId()}, Integer.class);

			if(exists >0) {
				template.update(updateQuery, account.getFirstName(), account.getLastName(), 
						String.valueOf(account.getMiddleInitial()), account.getJob(), account.getBalance(),account.getAccountId());
			}
			else {
				template.update(insertQuery,account.getAccountId(), account.getFirstName(), account.getLastName(), 
						String.valueOf(account.getMiddleInitial()), account.getJob(), account.getBalance());
			}
		}
		
	}
	
	

}
