package main.com.java.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;

import main.com.java.dao.PrepairedStatementSetter;
import main.com.java.model.Account;
import main.com.java.writer.AccountWriter;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private final JobBuilderFactory jobBuilderFacotry;
	
	private final StepBuilderFactory stepBuilderFactory;
	

	
	@Autowired 
	AccountWriter accountWriter;

	@Value("classpath*:/Account1.xml")
	public Resource[] inputFiles;
	
	public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		this.jobBuilderFacotry= jobBuilderFactory; 
		this.stepBuilderFactory = stepBuilderFactory;
	}
	
	@Bean
	public MultiResourceItemReader<Account> multiResourceItemReader(){
		MultiResourceItemReader<Account> multiResourceItemReader = new MultiResourceItemReader<>();
		multiResourceItemReader.setDelegate(reader());
		multiResourceItemReader.setResources(inputFiles);
		return multiResourceItemReader;
	}
	
	@Bean 
	public StaxEventItemReader<Account> reader(){
		StaxEventItemReader <Account> accountReader = new StaxEventItemReader<Account>();
		Map<String, String> map = new HashMap<>();
		//https://stackoverflow.com/questions/49450397/vulnerability-warning-with-xstreammarshaller
		map.put("Account", "main.com.java.model.Account");

		XStreamMarshaller  marshaller = new XStreamMarshaller();
		marshaller.getXStream().allowTypes(new Class[]{Account.class});
		/*https://www.baeldung.com/xstream-serialize-object-to-xml
		 Link above helped solve issue with com.thoughtworks.xstream.mapper.CannotResolveClassException with 
		 XStream 1.4.19
		 processAnnotations and @XStreamAlias on the class we are reading are both required to 
		 properly have the xStream work with latest version
		*/
		marshaller.getXStream().processAnnotations(Account.class);
		marshaller.setAliases(map);
		accountReader.setFragmentRootElementName("Account");
		accountReader.setUnmarshaller(marshaller);
		
		return accountReader;
	}
	
	
	@Bean
	public Job job() {
		return jobBuilderFacotry.get("fileJob").start(fileStep()).build();
		
		
	}
	
	//Reader and processor and we remove the writer. 
	//we are then goign to create 2 tasklets, those tasklets will either update or 
	
	
	@Bean
	public Step fileStep() {
		return this.stepBuilderFactory.get("fileStep").<Account,Account>chunk(5).reader(multiResourceItemReader())
			.writer(accountWriter).build();
	}
	
	/*
	@Bean 
	public ItemWriter<Account> accountWriter(){
		JdbcBatchItemWriter<Account> accountItemWriter = new JdbcBatchItemWriter<Account>();
		accountItemWriter.setDataSource(dataSource);
		accountItemWriter.setSql(insertQuery);
		accountItemWriter.setItemPreparedStatementSetter(new PrepairedStatementSetter());
		return accountItemWriter;
	}
	*/
	
	
	
}
