package com.example.kt2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ShopDB";
    private static final int DATABASE_VERSION = 1;

    // Bảng Sản phẩm
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_PRODUCT_ID = "id";
    private static final String COLUMN_PRODUCT_NAME = "name";
    private static final String COLUMN_PRODUCT_PRICE = "price";
    private static final String COLUMN_PRODUCT_IMAGE = "image";
    private static final String COLUMN_PRODUCT_DESCRIPTION = "description";

    // Bảng Khách hàng
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String COLUMN_CUSTOMER_ID = "id";
    private static final String COLUMN_CUSTOMER_NAME = "name";
    private static final String COLUMN_CUSTOMER_PHONE = "phone";

    // Bảng Hóa đơn
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "id";
    private static final String COLUMN_ORDER_CUSTOMER_ID = "customer_id";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_ORDER_TOTAL = "total";

    // Bảng Chi tiết hóa đơn
    private static final String TABLE_ORDER_DETAILS = "order_details";
    private static final String COLUMN_ORDER_DETAIL_ID = "id";
    private static final String COLUMN_ORDER_DETAIL_ORDER_ID = "order_id";
    private static final String COLUMN_ORDER_DETAIL_PRODUCT_ID = "product_id";
    private static final String COLUMN_ORDER_DETAIL_QUANTITY = "quantity";
    private static final String COLUMN_ORDER_DETAIL_PRICE = "price";

    // SQL tạo bảng Sản phẩm
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PRODUCT_NAME + " TEXT,"
            + COLUMN_PRODUCT_PRICE + " REAL,"
            + COLUMN_PRODUCT_IMAGE + " TEXT,"
            + COLUMN_PRODUCT_DESCRIPTION + " TEXT"
            + ")";

    // SQL tạo bảng Khách hàng
    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE " + TABLE_CUSTOMERS + "("
            + COLUMN_CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CUSTOMER_NAME + " TEXT,"
            + COLUMN_CUSTOMER_PHONE + " TEXT"
            + ")";

    // SQL tạo bảng Hóa đơn
    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_CUSTOMER_ID + " INTEGER,"
            + COLUMN_ORDER_DATE + " DATETIME,"
            + COLUMN_ORDER_TOTAL + " REAL,"
            + "FOREIGN KEY(" + COLUMN_ORDER_CUSTOMER_ID + ") REFERENCES "
            + TABLE_CUSTOMERS + "(" + COLUMN_CUSTOMER_ID + ")"
            + ")";

    // SQL tạo bảng Chi tiết hóa đơn
    private static final String CREATE_TABLE_ORDER_DETAILS = "CREATE TABLE " + TABLE_ORDER_DETAILS + "("
            + COLUMN_ORDER_DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_DETAIL_ORDER_ID + " INTEGER,"
            + COLUMN_ORDER_DETAIL_PRODUCT_ID + " INTEGER,"
            + COLUMN_ORDER_DETAIL_QUANTITY + " INTEGER,"
            + COLUMN_ORDER_DETAIL_PRICE + " REAL,"
            + "FOREIGN KEY(" + COLUMN_ORDER_DETAIL_ORDER_ID + ") REFERENCES "
            + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "),"
            + "FOREIGN KEY(" + COLUMN_ORDER_DETAIL_PRODUCT_ID + ") REFERENCES "
            + TABLE_PRODUCTS + "(" + COLUMN_PRODUCT_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo các bảng
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);

        // Tạo lại các bảng
        onCreate(db);
    }

    // Methods for Products
    public long addProduct(String name, double price, String image, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE, image);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
    }

    // Methods for Customers
    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, name);
        values.put(COLUMN_CUSTOMER_PHONE, phone);
        return db.insert(TABLE_CUSTOMERS, null, values);
    }

    // Methods for Orders
    public long createOrder(long customerId, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_CUSTOMER_ID, customerId);
        values.put(COLUMN_ORDER_DATE, getCurrentDateTime());
        values.put(COLUMN_ORDER_TOTAL, total);
        return db.insert(TABLE_ORDERS, null, values);
    }

    // Methods for Order Details
    public void addOrderDetail(long orderId, long productId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_DETAIL_ORDER_ID, orderId);
        values.put(COLUMN_ORDER_DETAIL_PRODUCT_ID, productId);
        values.put(COLUMN_ORDER_DETAIL_QUANTITY, quantity);
        values.put(COLUMN_ORDER_DETAIL_PRICE, price);
        db.insert(TABLE_ORDER_DETAILS, null, values);
    }

    private String getCurrentDateTime() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }

    // Products
    public Cursor getProductById(long productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null, null, null);
    }

    public void updateProduct(long productId, String name, double price, String image, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE, image);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        db.update(TABLE_PRODUCTS, values,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});
    }

    public void deleteProduct(long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});
    }

    // Customers
    public Cursor getCustomerById(long customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null,
                COLUMN_CUSTOMER_ID + " = ?",
                new String[]{String.valueOf(customerId)},
                null, null, null);
    }

    public Cursor getAllCustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null, null, null, null, null, null);
    }

    // Orders
    public Cursor getOrderById(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.*, c.name as customer_name, c.phone as customer_phone, " +
                "p.name as product_name, od.price, od.quantity " +
                "FROM " + TABLE_ORDERS + " o " +
                "JOIN " + TABLE_CUSTOMERS + " c ON o.customer_id = c.id " +
                "JOIN " + TABLE_ORDER_DETAILS + " od ON o.id = od.order_id " +
                "JOIN " + TABLE_PRODUCTS + " p ON od.product_id = p.id " +
                "WHERE o.id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT o.*, c.name as customer_name " +
                "FROM " + TABLE_ORDERS + " o " +
                "JOIN " + TABLE_CUSTOMERS + " c ON o.customer_id = c.id";
        return db.rawQuery(query, null);
    }

    // Order Details
    public Cursor getOrderDetails(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDER_DETAILS, null,
                COLUMN_ORDER_DETAIL_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)},
                null, null, null);
    }

    // Utility methods
    public boolean deleteOrder(long orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete order details first
            db.delete(TABLE_ORDER_DETAILS,
                    COLUMN_ORDER_DETAIL_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            // Then delete the order
            int result = db.delete(TABLE_ORDERS,
                    COLUMN_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    public double calculateOrderTotal(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(quantity * price) as total FROM " + TABLE_ORDER_DETAILS +
                " WHERE " + COLUMN_ORDER_DETAIL_ORDER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        if (cursor.moveToFirst()) {
            double total = cursor.getDouble(cursor.getColumnIndex("total"));
            cursor.close();
            return total;
        }
        cursor.close();
        return 0;
    }

    public void updateOrderTotal(long orderId) {
        double total = calculateOrderTotal(orderId);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_TOTAL, total);
        db.update(TABLE_ORDERS, values,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
    }
    // Thêm phương thức này vào lớp DatabaseHelper
    public Order.LocalOrder getOrder(long orderId) {
        Order.LocalOrder order = null;
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Truy vấn SQL để lấy thông tin đơn hàng, khách hàng và sản phẩm
            String sql = "SELECT o.id, o.order_date, o.total, " +
                    "c.name AS customer_name, c.phone AS customer_phone, " +
                    "p.name AS product_name, od.price, od.quantity " +
                    "FROM orders o " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN order_details od ON od.order_id = o.id " +
                    "JOIN products p ON od.product_id = p.id " +
                    "WHERE o.id = ?";

            Cursor cursor = db.rawQuery(sql, new String[] { String.valueOf(orderId) });

            if (cursor.moveToFirst()) {
                order = new Order.LocalOrder();
                order.setId(cursor.getLong(cursor.getColumnIndex("id")));
                order.setOrderDate(cursor.getString(cursor.getColumnIndex("order_date")));
                order.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                order.setCustomerName(cursor.getString(cursor.getColumnIndex("customer_name")));
                order.setCustomerPhone(cursor.getString(cursor.getColumnIndex("customer_phone")));
                order.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
                order.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                order.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting order: " + e.getMessage());
        } finally {
            db.close();
        }

        return order;
    }
}
