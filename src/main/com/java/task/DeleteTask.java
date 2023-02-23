package main.com.java.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

public class DeleteTask  implements Tasklet{

	@Value("${files.processLocation}")
	public String processLocation;
	
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		FileUtils.deleteDirectory(new File (processLocation));
		return RepeatStatus.FINISHED;
	}

}
