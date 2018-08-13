import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.actions.Photos;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageThread implements Runnable {
    List<JsonObject> list;
    VkApiClient apiClient;
    GroupActor actor;
    ServiceActor userActor = new ServiceActor(6502666, "733dfe5e733dfe5e733dfe5e6a735ec7547733d733dfe5e2824c85a408c46387176e199");
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/users?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String MYSQL_LOGIN = "root";
    private static final String MYSQL_PASSWORD = "root";
    private static int userId;


    Connection connection;
    Statement statement;


    public MessageThread(List<JsonObject> list, VkApiClient apiClient, GroupActor actor) {
        this.list = list;
        this.apiClient = apiClient;
        this.actor = actor;

        try {
//                Class.forName("com.mysql.jdbc.Drive");
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        Random random = new Random();

        for (JsonObject js : list) {
            JsonObject jsonObject = js.getAsJsonObject("object");
            String messageFromUser = jsonObject.get("body").getAsString();
            JsonElement photoFromUser;
            try {
                photoFromUser = jsonObject.get("attachments").getAsJsonArray();
            } catch (NullPointerException e) {
                photoFromUser = null;
            }
             userId = jsonObject.get("user_id").getAsInt();
            boolean isUserExists = false;
            int countByDay = 0;

            try {
                ResultSet resultSet = statement.executeQuery("select * from freeusers where user_id =" + userId + " LIMIT 1");

                if (resultSet.next()) {
                    isUserExists = true;
                }
                if (!isUserExists) {
                    sendTheMessage("Необходимо оформить подписку, за всеми подробностями обращайтесь к администратору",userId,actor);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            if (photoFromUser != null) {
                if (((JsonArray) photoFromUser).size() == 1) {
                    JsonArray jsAttachment = jsonObject.getAsJsonArray("attachments");
                    Iterator phonesItr = jsAttachment.iterator();
                    JsonObject firstJS = (JsonObject) phonesItr.next();
                    JsonObject jsPhoto = firstJS.getAsJsonObject("photo");
                    List<String> attachIdList = (listOfIdFromSearch(jsPhoto.get("photo_604").getAsString(), jsonObject.get("user_id").getAsString()));
                    if (messageFromUser.length() < 1) {
                        sendTheMessageWithAttach("Свежая подборка", userId, actor, attachIdList);
                    } else {
                        sendTheMessageWithAttach("Моя не понимать текст, но моя уметь делать подборка", userId, actor, attachIdList);

                    }
                }
                if (((JsonArray) photoFromUser).size() > 1) {
                    sendTheMessage("Слишком мощно,давай по одной фотке!", userId, actor);

                }

            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadPhotoInListForSend(ArrayList<String> listFromDB) {
        List<String> list = new ArrayList<>();
        if (listFromDB.size() > 0) {
            for (int i = 0; i < 10; i++) {
                File file = null;
                list.add("photo" + -104375368 + "_" + listFromDB.get(i));
            }
        }

        return list;
    }

    private ArrayList<String> listOfIdFromSearch(String URL, String user_id) {
        Searcher searcher = null;
        File file = null;
        BufferedImage img = null;
        String fileName = null;
        File folder = new File(Main.userPhotoFolderPath + user_id);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            for (File myFile : new File(Main.userPhotoFolderPath + user_id).listFiles())
                if (myFile.isFile()) myFile.delete();
            fileName = Main.userPhotoFolderPath + user_id + "\\" + user_id + "&1.png";
            img = ImageIO.read(new URL(URL));
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();

            }
            ImageIO.write(img, "png", file);



        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            searcher = new Searcher(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }





        return listOfIdWithFilters(searcher,user_id);
    }
    private ArrayList<String> listOfIdWithFilters(Searcher searcher,String user_id){
        boolean isLoaded = true;
        ArrayList<File> listOfFiles= new ArrayList<>();
        ArrayList<String> listOfIds=new ArrayList<>();


        File folder = new File("C:\\Users\\Staff2\\Desktop\\PhotoForFilters\\" + user_id);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {


            ArrayList<String> listOfPhotoForFilters = new ArrayList<>();
            for(String s:searcher.getIdList()){
                listOfPhotoForFilters.add(s.replaceAll("\\.png","").replaceAll("photo",""));
            }
            List<Photo> listOfPhoto=apiClient.photos().getById(userActor,listOfPhotoForFilters).execute();
            try {

                for (int i = 0; i < listOfPhoto.size(); i++) {
                    String fileName = "C:\\Users\\Staff2\\Desktop\\PhotoForFilters\\"+user_id+"\\photo+"+i+ ".png";

                    if (listOfPhoto.get(i).getPhoto1280() != null) {
                        BufferedImage img = ImageIO.read(new URL(listOfPhoto.get(i).getPhoto1280()));
                        if (isLoaded) {
                            File file = new File(fileName);
                            applyEffectToBitmap(file,new File(Main.listOfFilters.get(1)));
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            ImageIO.write(img, "png", file);

                        }
                    }
                }

                for (File myFile : new File("C:\\Users\\Staff2\\Desktop\\testFilterPhoto\\"+ userId).listFiles()){
                    if (myFile.isFile()) {
                        listOfFiles.add(myFile);
                    }
                }
                for (File file: listOfFiles) {
                    PhotoUpload photoUpload = apiClient.photos().getMessagesUploadServer(actor).execute();
                    MessageUploadResponse messageUploadResponse = apiClient.upload().photoMessage(photoUpload.getUploadUrl(), file).execute();

                    List<Photo> photoList = apiClient.photos().saveMessagesPhoto(actor, messageUploadResponse.getPhoto())
                            .server(messageUploadResponse.getServer())
                            .hash(messageUploadResponse.getHash())
                            .execute();
                    Photo photo = photoList.get(0);
                    listOfIds.add("photo" + photo.getOwnerId() + "_" + photo.getId());
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }


        System.out.println();
        return listOfIds;
    }

    private ArrayList<String> listOfIdFromSearchForEshe(String user_id) {
        File file = new File(Main.userPhotoFolderPath + user_id);
        File[] files = file.listFiles();
        String name = files[0].getName();
        String[] namePart = name.split("&");
        Searcher searcher = null;
        try {
            searcher = new Searcher(files[0].getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> list = new ArrayList<>(searcher.getIdList());
        ArrayList<String> resultList;
        int numberOfEshe = Integer.parseInt(namePart[1].substring(0, 1));
        if (numberOfEshe == 1) {
            resultList = new ArrayList<>(list.subList(9, 19));
        } else if (numberOfEshe == 2) {
            resultList = new ArrayList<>(list.subList(19, 29));
        } else {
            resultList = new ArrayList<>();
        }
        numberOfEshe++;
        files[0].renameTo(new File(Main.userPhotoFolderPath + user_id + "\\" + user_id + "&" + numberOfEshe + ".png"));
        return resultList;

    }

    private void sendTheMessage(String text, int userId, GroupActor actor) {
        Random random = new Random();
        try {
            apiClient.messages()
                    .send(actor)
                    .message(text)
                    .userId(userId)
                    .randomId(random.nextInt())
                    .execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void sendTheMessageWithAttach(String text, int userId, GroupActor actor, List<String> attachIdList) {
        Random random = new Random();
        try {
            apiClient.messages()
                    .send(actor)
                    .message(text)
                    .userId(userId)
                    .randomId(random.nextInt())
                    .attachment(attachIdList)
                    .execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void reIndex() throws IOException {
        System.out.println("start");
        //Удаление дублей
//        List<String> allDuplicateImagesForDeletion = DuplicateImageFinder.findDuplicatesForDeletion("C:\\Users\\Staff2\\Desktop\\TestPhoto");
//        for (String s : allDuplicateImagesForDeletion) {
//            System.out.println(s);
//            File f = new File(s);
//            f.delete();
//
//        }

        //Индексация
        Indexer indexer = new Indexer(Main.photoFolderPath, Main.reIndexPath);

        //Удаление старого индекса
        for (File myFile : new File(Main.indexPath).listFiles())
            if (myFile.isFile()) myFile.delete();

        //Копирование нового индекса на место старого
        File folder = new File(Main.reIndexPath);

        File[] listOfFiles = folder.listFiles();

        Path destDir = Paths.get(Main.indexPath);
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                Files.copy(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }


    }

    /**
     * Check has a user 10 likes for last week
     *
     * @param actor
     * @param vk
     * @param user_id
     * @return true if has, no if hasnt
     * @throws ClientException
     * @throws ApiException
     * @throws InterruptedException
     */
    private boolean is10Likes(UserActor actor, VkApiClient vk, String user_id) throws ClientException, ApiException, InterruptedException {
//        int countOfLikes=0;
        AtomicInteger countOfLikes = new AtomicInteger(0);
        List<WallPostFull> list = new ArrayList<>();


        GetResponse getResponse = vk.wall().get(actor).ownerId(-104375368).count(100).execute();
        list.addAll(getResponse.getItems());
        long time = System.currentTimeMillis() / 1000;
        int offsetForPost = 0;

        while ((time - list.get(list.size() - 1).getDate()) < 604800) {
            getResponse = vk.wall().get(actor).ownerId(-104375368).count(50).offset(offsetForPost).execute();
            list.addAll(getResponse.getItems());
            offsetForPost += 50;
        }
        int start = 0;
        int mid = (list.size() / 3) - 1;
        int end = mid;
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 3; i++) {
            List<WallPostFull> l = new ArrayList<>(list.subList(start, end));
            threads.add(new Thread(new LikeFoundThread(countOfLikes, l, vk, actor, user_id)));
            start = end;
            end += mid + 1;
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        return countOfLikes.get() >= 10;
    }

    public static void applyEffectToBitmap(File file, File file2) throws IOException {
        //android specific code, storing the LUT image's pixels in an int array
        BufferedImage src = ImageIO.read(file);
        BufferedImage lutBitmap = ImageIO.read(file2);
        int lutWidth = lutBitmap.getWidth();
        int lutColors[] = new int[lutWidth * lutBitmap.getHeight()];
        lutBitmap.getRGB(0, 0, lutBitmap.getWidth(), lutBitmap.getHeight(), lutColors, 0, lutBitmap.getWidth());
//        lutBitmap.getPixels(lutColors, 0, lutWidth, 0, 0, lutWidth, lutBitmap.getHeight());


        //android specific code, storing the source image's pixels in an int array
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int[] pix = new int[srcWidth * srcHeight];

//        src.getPixels(pix, 0, srcWidth, 0, 0, srcWidth, srcHeight);
        src.getRGB(0, 0, srcWidth, srcHeight, pix, 0, srcWidth);
        int R, G, B;
        //now we can iterate through the pixels of the source image
        for (int y = 0; y < srcHeight; y++) {
            for (int x = 0; x < srcWidth; x++) {
                //index: because the pix[] is one dimensional
                int index = y * srcWidth + x;
                //pix[index] returns a color, we need it's r g b values, thats why the shift operator is used
                int r = ((pix[index] >> 16) & 0xff) / 4;
                int g = ((pix[index] >> 8) & 0xff) / 4;
                int b = (pix[index] & 0xff) / 4;

                //the magic happens here: the 3rd step above: blue pixel describes the
                //column and row of which square you'll need the pixel from
                //then simply add the r and green value to get the coordinates
                int lutX = (b % 8) * 64 + r;
                int lutY = (b / 8) * 64 + g;
                int lutIndex = lutY * lutWidth + lutX;


                //same pixel getting as above, but now from the lutColors int array
                R = ((lutColors[lutIndex] >> 16) & 0xff);
                G = ((lutColors[lutIndex] >> 8) & 0xff);
                B = ((lutColors[lutIndex]) & 0xff);


                //overwrite pix array with the filtered values, alpha is 256 in my case, but you can use the original alpha
                pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
            }
        }
        //at the end of operations pix[] has the transformed pixels sou can set them to your new bitmap
        //some android specific code is here for creating bitmaps
//        Bitmap result = Bitmap.createBitmap(srcWidth, srcHeight, src.getConfig());
//        result.setPixels(pix, 0, srcWidth, 0, 0, srcWidth, srcHeight);
        BufferedImage bufferedImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, srcWidth, srcHeight, pix, 0, srcWidth);
        File folder = new File("C:\\Users\\Staff2\\Desktop\\testFilterPhoto\\" + userId);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file3 = new File("C:\\Users\\Staff2\\Desktop\\testFilterPhoto\\"+ userId+"\\" + file.getName() + "_" + file2.getName() + ".png");
        try {
            ImageIO.write(bufferedImage, "png", file3);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
