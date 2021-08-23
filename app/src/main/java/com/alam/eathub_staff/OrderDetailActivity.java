package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alam.eathub_staff.Adapter.MyDetailOrderAdapter;
import com.alam.eathub_staff.Adapter.MyOrderAdapter;
import com.alam.eathub_staff.Adapter.PdfPrintDocumentAdapter;
import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Model.FCMSendData;
import com.alam.eathub_staff.Model.Order;
import com.alam.eathub_staff.Model.OrderDetail;
import com.alam.eathub_staff.Model.Status;
import com.alam.eathub_staff.Retrofit.IFCMService;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetroFitFCMClient;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.recycler_order_detail)
    RecyclerView recycler_order_detail;
    @BindView(R.id.spinner_status)
    AppCompatSpinner spinner_status;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String fileName = "Order_.pdf";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");

    IMyRestaurantAPI myRestaurantAPI;
    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;

    int maxData = 0;
    MyOrderAdapter adapter;
    List<Order> orderList;

    LayoutAnimationController layoutAnimationController;
    List<OrderDetail> orderDetailList = new ArrayList<>();

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            finish();
            return true;
        }
        else if(id==R.id.action_save){
            updateOrder();
            return true;
        }
        else if(id==R.id.action_print){
            printOrder();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void printOrder() {
        createPDFFile(Common.getAppPath(OrderDetailActivity.this)+fileName);
    }

    private void createPDFFile(String path) {
        if(new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();
            //save
            PdfWriter.getInstance(document , new FileOutputStream(path));
            //open to write
            document.open();

            //settings
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("EatHub");
            document.addCreator("vold");

            //font Setting
            BaseColor mColorAccent = new BaseColor(0,153,204,255);
            float mHeadingFontSize = 25.0f;
            float mValueFontSize = 20.0f;
            BaseFont uriName  =BaseFont.createFont("assets/fonts/magnifika.otf",BaseFont.IDENTITY_H , BaseFont.EMBEDDED);

            Font mOrderTitleFont = new Font(uriName , 25 , Font.NORMAL , BaseColor.BLACK);
            Font mOrderItemFont = new Font(uriName , 15 , Font.NORMAL , BaseColor.BLACK);
            Font mOrderItemFontBold = new Font(uriName , 15 , Font.BOLD , BaseColor.BLACK);
            Font mOrderIdFont = new Font(uriName , 10 , Font.NORMAL , mColorAccent);
            Font mOrderAddon = new Font(uriName , 10 , Font.ITALIC , mColorAccent);

            //Top document
            addNewItemWithLeftAndRight(document , String.valueOf(Common.currentOrder.getOrderId())
                    , simpleDateFormat.format(Common.currentOrder.getOrderDate()) , mOrderItemFont , mOrderIdFont);

            addNewItem(document , "EatHub" , Element.ALIGN_CENTER , mOrderTitleFont);
            addLineSeperator(document);

            //Name
            addNewItem(document , Common.currentOrder.getOrderName() , Element.ALIGN_CENTER ,mOrderItemFontBold);
            addNewItem(document , Common.currentOrder.getOrderAddress() , Element.ALIGN_CENTER ,mOrderItemFontBold);
            addNewItem(document , Common.currentOrder.getOrderPhone() , Element.ALIGN_CENTER ,mOrderItemFontBold);
            addLineSeperator(document);

            //cart items
            for(OrderDetail item:orderDetailList){
                addNewItem(document , new StringBuilder("").append(item.getQuantity()).append("x ").append(item.getName()).toString() , Element.ALIGN_LEFT , mOrderItemFont);
                if(!TextUtils.isEmpty(item.getSize()))
                    addNewItem(document , new StringBuilder("").append(item.getSize()).toString() , Element.ALIGN_LEFT , mOrderAddon);

                if(!TextUtils.isEmpty(item.getAddOn()))
                    addNewItem(document , new StringBuilder("").append(item.getAddOn()).toString() , Element.ALIGN_LEFT , mOrderAddon);

                addLineSeperator(document);
            }

            document.close();
            Toast.makeText(this, "Create Pdf success", Toast.LENGTH_SHORT).show();
            printPdf();

        }catch (FileNotFoundException | DocumentException e){e.printStackTrace();} catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPdf() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfPrintDocumentAdapter(OrderDetailActivity.this , Common.getAppPath(OrderDetailActivity.this)+fileName);
            printManager.print("Document" , printDocumentAdapter , new PrintAttributes.Builder().build());
        }
        catch (Exception e){
            Log.e("ERROR" , ""+e.getMessage());
        }
    }

    private void addLineSeperator(Document document) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(new BaseColor(0,0,0,68));
        addLineSpace(document);
        document.add(new Chunk(lineSeparator));
        addLineSpace(document);
    }

    private void addLineSpace(Document document) throws DocumentException {
        document.add(new Paragraph(""));
    }

    private void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text , font);
        Paragraph p = new Paragraph(chunk);
        p.setAlignment(align);
        document.add(p);
    }

    private void addNewItemWithLeftAndRight(Document document, String leftText, String rightText, Font leftFont, Font rightFont) throws DocumentException {
        Chunk chunkLeftText = new Chunk(leftText , leftFont);
        Chunk chunkRightText = new Chunk(rightText , rightFont);
        Paragraph p = new Paragraph(chunkLeftText);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(chunkRightText);
        document.add(p);
    }

    private void updateOrder() {
        int status = Common.convertStringToStatus(spinner_status.getSelectedItem().toString());
        if(status ==1 ){
            Map<String , String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
            compositeDisposable.add(myRestaurantAPI.updateOrderStatus(headers, Common.currentOrder.getOrderId(), Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateOrderModel -> {
                        Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));

                        compositeDisposable.add(myRestaurantAPI.setShippingOrder(headers, Common.currentOrder.getOrderId() , Common.currentRestaurantOwner.getRestaurantId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(shipperOrderModel -> {
                                            if(shipperOrderModel.isSuccess()){
                                                //get token to send notification

                                                compositeDisposable.add(myRestaurantAPI.getToken(headers, Common.currentOrder.getOrderFBID())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(tokenModel -> {
                                                            if (tokenModel.isSuccess()) {
                                                                Map<String, String> messageSend = new HashMap<>();
                                                                messageSend.put(Common.NOTIFI_TITLE, "Your Order has been updated");
                                                                messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Your order")
                                                                        .append(Common.currentOrder.getOrderId())
                                                                        .append("has been updated to")
                                                                        .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                                                FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);

                                                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(fcmResponse -> {
                                                                            Toast.makeText(this, "Order Updated!!", Toast.LENGTH_SHORT).show();
                                                                        }, throwable -> {
                                                                            Toast.makeText(this, "Order updated but cant send notification", Toast.LENGTH_SHORT).show();
                                                                        }));
                                                            }
                                                        }, throwable -> {
                                                            Toast.makeText(this, "[GET TOKEN]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                        })
                                                );

                                            }
                                            else{
                                                Toast.makeText(this, "[SET SHIPPER]"+shipperOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        } , throwable -> {
                                            Toast.makeText(this, "[SET SHIPPER API]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                        );
                    }, throwable -> {
                        Toast.makeText(this, "[UPDATE ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    })
            );
        }
        else {
            Map<String , String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
            compositeDisposable.add(myRestaurantAPI.updateOrderStatus(headers, Common.currentOrder.getOrderId(), Common.convertStringToStatus(spinner_status.getSelectedItem().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(updateOrderModel -> {
                        Common.currentOrder.setOrderStatus(Common.convertStringToStatus(spinner_status.getSelectedItem().toString()));
                        compositeDisposable.add(myRestaurantAPI.getToken(headers, Common.currentOrder.getOrderFBID())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(tokenModel -> {
                                    if (tokenModel.isSuccess()) {
                                        Map<String, String> messageSend = new HashMap<>();
                                        messageSend.put(Common.NOTIFI_TITLE, "Your Order has been updated");
                                        messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Your order")
                                                .append(Common.currentOrder.getOrderId())
                                                .append("has been updated to")
                                                .append(Common.convertStatusToString(Common.currentOrder.getOrderStatus())).toString());

                                        FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);

                                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(fcmResponse -> {
                                                    Toast.makeText(this, "Order Updated!!", Toast.LENGTH_SHORT).show();
                                                }, throwable -> {
                                                    Toast.makeText(this, "Order updated but cant send notification", Toast.LENGTH_SHORT).show();
                                                }));
                                    }
                                }, throwable -> {
                                    Toast.makeText(this, "[GET TOKEN]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                        );


                    }, throwable -> {
                        Toast.makeText(this, "[UPDATE ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    })
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.order_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_order_detail.setLayoutManager(layoutManager);
        recycler_order_detail.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        txt_order_number.setText(new StringBuilder("Order Number :").append(Common.currentOrder.getOrderId()));

        initStatusSpinner();
        loadOrderDetail();
    }

    private void loadOrderDetail() {
        dialog.show();

        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.getOrderDetail(headers ,Common.currentOrder.getOrderId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderDetailModel -> {
                    if(orderDetailModel.isSuccess()){
                        if(orderDetailModel.getResult().size() > 0){
                            orderDetailList = orderDetailModel.getResult();
                            MyDetailOrderAdapter adapter = new MyDetailOrderAdapter(this , orderDetailList);
                            recycler_order_detail.setAdapter(adapter);
                        }
                    }
                    dialog.dismiss();
                } , throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET ORDER DETAIL]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void initStatusSpinner() {
        List<Status> statusList  = new ArrayList<Status>();

        statusList.add(new Status(0 , "Placed"));
        statusList.add(new Status(1 , "Shipping"));
        //statusList.add(new Status(2 , "Shipped"));
        statusList.add(new Status(-1 , "Cancelled"));

        ArrayAdapter adapter = new ArrayAdapter(this , android.R.layout.simple_spinner_item , statusList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_status.setAdapter(adapter);
        spinner_status.setSelection(Common.convertStatusToIndex(Common.currentOrder.getOrderStatus()));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
        ifcmService = RetroFitFCMClient.getInstance().create(IFCMService.class);

    }

}