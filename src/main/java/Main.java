import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


public class Main {


    private static final int GROUP_ID = 167356546;
    private static final String TOKEN = "419456ea8317c9b6c248041212dc3dd36189bd49440f37d0be47d94cee53441defa5c6efa0d4c372e24cd";
    private final static Random random = new Random();
    public static HashSet<String> adminIds = new HashSet<>();
    public static String indexPath = "C:\\Users\\Staff2\\Desktop\\index";
    public static String reIndexPath = "C:\\Users\\Staff2\\Desktop\\reIndex";
    public static String photoFolderPath = "C:\\Users\\Staff2\\Desktop\\TestPhoto";
    public static String userPhotoFolderPath = "C:\\Users\\Staff2\\Desktop\\PhotoFromUser\\";
    public static String filtersPhotoPath = "C:\\Users\\Staff2\\Desktop\\filters\\";
    public static HashMap<Integer,String> listOfFilters = new HashMap();

    public static void main(String[] args) throws ClientException, ApiException, IOException, SchedulerException {
        HttpTransportClient client = new HttpTransportClient();
        VkApiClient apiClient = new VkApiClient(client);
        GroupActor actor = new GroupActor(GROUP_ID, TOKEN);

//         Lut Filters

//        for (File myFile : new File("C:\\Users\\Staff2\\Desktop\\filters").listFiles()){
//            if (myFile.isFile()) {
//                File file = new File("C:\\Users\\Staff2\\Desktop\\testFilterPhoto\\origin.png");
//                applyEffectToBitmap(file,myFile);
//            }
//        }

//


        dailyReset();
        initAdmins();
        initFilters(listOfFilters);

        GetLongPollServerResponse getResponse = apiClient.groups().getLongPollServer(actor).execute();

        int ts = getResponse.getTs();

        while (true) {
            GetLongPollEventsResponse getLongPollServerResponse = null;
            List<JsonObject> list = null;
            try {
                getLongPollServerResponse = apiClient.longPoll().getEvents(getResponse.getServer(), getResponse.getKey(), ts).waitTime(25).execute();
                list = getLongPollServerResponse.getUpdates();
            } catch (Exception e) {
                getResponse = apiClient.groups().getLongPollServer(actor).execute();
            }
            if (list != null && !list.isEmpty()) {
                System.out.println("here");
                new Thread(new MessageThread(list, apiClient, actor)).start();
                ts = getLongPollServerResponse.getTs();
            }

        }


    }
    private static void initAdmins(){
        adminIds.add("2331487");

    }

    public static void dailyReset() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        JobDetail job = newJob(JobChecker.class)
                .withIdentity("job1", "group1")
                .build();
// compute a time that is on the next round minute
        Date runTime = evenMinuteDate(new Date());

// Trigger the job to run on the next round minute
        Trigger trigger = newTrigger()
                .withIdentity("trigger3", "group1")
                .withSchedule(dailyAtHourAndMinute(7, 0))
                .build();
        // Tell quartz to schedule the job using our trigger
        sched.scheduleJob(job, trigger);
        sched.start();

    }
    public static void initFilters(HashMap<Integer,String> map){
        map.put(0,"default");
        map.put(1,filtersPhotoPath+"\\alvarez.png");
        map.put(2,filtersPhotoPath+"\\Aspen.png");
        map.put(3,filtersPhotoPath+"\\blackWhite.png");
        map.put(4,filtersPhotoPath+"\\cinematic.png");
        map.put(5,filtersPhotoPath+"\\cleanBlue.png");

    }
}
