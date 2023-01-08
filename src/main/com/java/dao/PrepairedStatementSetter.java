package main.com.java.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import main.com.java.model.Account;
import main.com.java.model.Address;

public class PrepairedStatementSetter implements ItemPreparedStatementSetter<Account> {

	@Override
	public void setValues(Account account, PreparedStatement ps) throws SQLException {
		ps.setString(1, account.getAccountId());
		ps.setString(2, account.getFirstName());
		ps.setString(3, account.getLastName());
		ps.setString(4, String.valueOf(account.getMiddleInitial()));
		ps.setString(5, account.getJob());
		ps.setDouble(6,account.getBalance());
		
	}

	
	
	
	
}
