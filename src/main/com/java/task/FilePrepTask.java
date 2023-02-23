package main.com.java.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilePrepTask implements Tasklet {

	
	@Value("${files.initalLocation}")
	private String initalLocation;
	
	@Value("${files.processLocation}")
	public String processLocation;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		FileUtils.copyDirectory(new File (initalLocation), new File(processLocation));
		return RepeatStatus.FINISHED;
	}

}
