package main.com.java.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.core.io.Resource;

import main.com.java.model.Account;
import main.com.java.task.DeleteTask;
import main.com.java.task.FilePrepTask;
import main.com.java.writer.AccountWriter;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	
	private final StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	ResourceLoader loader;
	
	@Value("${files.processLocation}")
	public String processLocation;
	
	/*
	 * Unable to find a way to get this to work with creating a process directory created 
	 * after the program starts. 
	@Value("file:${files.initalLocation}" + "/Account*.xml")
	public Resource[] inputFiles;
	*/
	public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
		this.jobBuilderFactory= jobBuilderFactory; 
		this.stepBuilderFactory = stepBuilderFactory;
	}
	
	@Bean
	@StepScope
	public MultiResourceItemReader<Account> multiResourceItemReader(){
		MultiResourceItemReader<Account> multiResourceItemReader = new MultiResourceItemReader<>();
		multiResourceItemReader.setDelegate(reader());
		List<FileSystemResource> processingFiles = new ArrayList<>();
		Stream<Path> files = null;
		try {
			files = Files.list(Paths.get(processLocation));
			files.forEach(file ->{
				processingFiles.add(new FileSystemResource(file.toFile()));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			files.close();
		}
		Resource[] resources = {};
		resources = processingFiles.toArray(resources);
		multiResourceItemReader.setResources(resources);
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
		return jobBuilderFactory.get("fileJob").start(prepFilesStep()).next(fileStep()).next(deleteProcessedFilesStep()).build();
	}
	
	@Bean
	public Step prepFilesStep() {
		return this.stepBuilderFactory.get("prepFilesStep").tasklet(getFilePrepTask()).build();
	}

	@Bean
	public Step deleteProcessedFilesStep() {
		return this.stepBuilderFactory.get("deleteProcessedFilesStep").tasklet(getDeleteTask()).build();
	}
	
	@Bean
	@StepScope
	public FilePrepTask getFilePrepTask() {
		return new FilePrepTask();
	}
	
	@Bean
	@StepScope
	public DeleteTask getDeleteTask() {
		return new DeleteTask();
	}
	
	@Bean
	public Step fileStep() {
		return this.stepBuilderFactory.get("fileStep").<Account,Account>chunk(5).reader(multiResourceItemReader())
			.writer( getAccountWriter()).build();
	}
	
	@Bean
	@StepScope
	public AccountWriter getAccountWriter() {
		return new  AccountWriter();
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
