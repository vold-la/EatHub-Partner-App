package com.alam.eathub_staff.Retrofit;

import com.alam.eathub_staff.Model.GetKeyModel;
import com.alam.eathub_staff.Model.HotFoodModel;
import com.alam.eathub_staff.Model.MaxOrderModel;
import com.alam.eathub_staff.Model.OrderDetailModel;
import com.alam.eathub_staff.Model.OrderModel;
import com.alam.eathub_staff.Model.RestaurantOwnerModel;
import com.alam.eathub_staff.Model.ShipperModel;
import com.alam.eathub_staff.Model.ShipperOrderModel;
import com.alam.eathub_staff.Model.TokenModel;
import com.alam.eathub_staff.Model.UpdateOrderModel;
import com.alam.eathub_staff.Model.UpdateRestaurantOwnerModel;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IMyRestaurantAPI {


    @GET("key")
    Observable<GetKeyModel> getKey(@Query("fbid") String fbid);

    @GET("shipping/restaurant")
    Observable<ShipperModel> getShipperByRestaurant(@HeaderMap Map<String , String> headers ,
                                                    @Query("restaurantId") int restaurantId);

    @GET("owner")
    Observable<RestaurantOwnerModel> getRestaurantOwner(@HeaderMap Map<String , String> headers );

    @GET("restaurant/orders")
    Observable<OrderModel> getOrder(@HeaderMap Map<String , String> headers,
                                    @Query("restaurantId") String restaurantId ,
                                    @Query("from") int from ,
                                    @Query("to") int to);

    @GET("restaurant/maorder")
    Observable<MaxOrderModel> getMaxOrder(@HeaderMap Map<String , String> headers,
                                          @Query("restaurantId") String restaurantId);

    @GET("restaurant/detail")
    Observable<OrderDetailModel> getOrderDetail(@HeaderMap Map<String , String> headers,
                                                @Query("orderId") int orderId);

    @GET("token")
    Observable<TokenModel> getToken(@HeaderMap Map<String , String> headers,
                                    @Query("fbid") String orderFBID);

    @GET("order/hotfood")
    Observable<HotFoodModel> getHotFood(@HeaderMap Map<String , String> headers);

    @GET("shipping")
    Observable<ShipperOrderModel> getShippingOrder(@HeaderMap Map<String , String> headers,
                                                    @Query("restaurantId") int restaurantId,
                                                   @Query("from") int from ,
                                                   @Query("to") int to);

    @GET("shipping/maxorder")
    Observable<MaxOrderModel> getMaxOrderNeedShip(@HeaderMap Map<String , String> headers,
                                                  @Query("restaurantId") int restaurantId );


    //POST

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@HeaderMap Map<String , String> headers,
                                               @Field("token") String token);

    @POST("order")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrderStatus(@HeaderMap Map<String , String> headers,
                                                   @Field("orderId") int orderId ,
                                                   @Field("orderStatus") int orderStatus);


    @POST("shipping/shipper")
    @FormUrlEncoded
    Observable<ShipperModel> updateShipper(@HeaderMap Map<String , String> headers,
                                           @Field("restaurantId") int restaurantId ,
                                           @Field("enable") boolean enable );


    @POST("owner")
    @FormUrlEncoded
    Observable<UpdateRestaurantOwnerModel> updateRestaurantOwner(@HeaderMap Map<String , String> headers,
                                                                 @Field("userPhone") String userPhone ,
                                                                 @Field("userName") String userName );

    @POST("shipping")
    @FormUrlEncoded
    Observable<ShipperOrderModel> setShippingOrder(@HeaderMap Map<String , String> headers,
                                                   @Field("orderId") int orderId ,
                                                   @Field("restaurantId") int restaurantId);



}
