import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.likes.responses.GetListResponse;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.queries.likes.LikesGetListFilter;
import com.vk.api.sdk.queries.likes.LikesType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LikeFoundThread implements Runnable {
    AtomicInteger countOfLikes;
    List<WallPostFull> list;
    VkApiClient vk;
    UserActor actor;
    String user_id;

    public LikeFoundThread(AtomicInteger countOfLikes, List<WallPostFull> list, VkApiClient vk, UserActor actor, String user_id) {
        this.countOfLikes = countOfLikes;
        this.list = list;
        this.vk = vk;
        this.actor = actor;
        this.user_id = user_id;
    }

    @Override
    public void run() {
        for (int j = 0; j < list.size() || countOfLikes.get()<10; j++) {
            WallPostFull l = list.get(j);
            int offset = 0;
            List<Integer> listOfId = new ArrayList<>();
            float count = l.getLikes().getCount();
            double count2 = Math.ceil(count / 1000);
            for (int i = 0; i < (int) count2; i++) {
                GetListResponse getListResponse = null;
                try {
                    getListResponse = vk.likes().getList(actor, LikesType.POST).itemId(l.getId()).ownerId(-104375368).filter(LikesGetListFilter.LIKES).count(1000).offset(offset).execute();
                } catch (ApiException e) {
                    e.printStackTrace();
                } catch (ClientException e) {
                    e.printStackTrace();
                }
                listOfId.addAll(getListResponse.getItems());
                offset += 1000;
            }
            if (listOfId.contains(Integer.parseInt(user_id))) {
                countOfLikes.incrementAndGet();
            }
            System.out.println("end of 1 iter");


        }
    }
}
