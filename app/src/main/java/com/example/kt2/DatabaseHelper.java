package com.example.kt2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "ShopDB.db"; // Thêm .db để rõ ràng hơn
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
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + "("
            + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PRODUCT_NAME + " TEXT NOT NULL,"
            + COLUMN_PRODUCT_PRICE + " REAL NOT NULL,"
            + COLUMN_PRODUCT_IMAGE + " TEXT,"
            + COLUMN_PRODUCT_DESCRIPTION + " TEXT"
            + ")";

    // SQL tạo bảng Khách hàng
    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOMERS + "("
            + COLUMN_CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CUSTOMER_NAME + " TEXT NOT NULL,"
            + COLUMN_CUSTOMER_PHONE + " TEXT NOT NULL"
            + ")";

    // SQL tạo bảng Hóa đơn
    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + "("
            + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_CUSTOMER_ID + " INTEGER NOT NULL,"
            + COLUMN_ORDER_DATE + " TEXT NOT NULL,"
            + COLUMN_ORDER_TOTAL + " REAL NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_ORDER_CUSTOMER_ID + ") REFERENCES "
            + TABLE_CUSTOMERS + "(" + COLUMN_CUSTOMER_ID + ")"
            + ")";

    // SQL tạo bảng Chi tiết hóa đơn
    private static final String CREATE_TABLE_ORDER_DETAILS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_DETAILS + "("
            + COLUMN_ORDER_DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_DETAIL_ORDER_ID + " INTEGER NOT NULL,"
            + COLUMN_ORDER_DETAIL_PRODUCT_ID + " INTEGER NOT NULL,"
            + COLUMN_ORDER_DETAIL_QUANTITY + " INTEGER NOT NULL,"
            + COLUMN_ORDER_DETAIL_PRICE + " REAL NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_ORDER_DETAIL_ORDER_ID + ") REFERENCES "
            + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "),"
            + "FOREIGN KEY(" + COLUMN_ORDER_DETAIL_PRODUCT_ID + ") REFERENCES "
            + TABLE_PRODUCTS + "(" + COLUMN_PRODUCT_ID + ")"
            + ")";

    private SimpleDateFormat dateFormat;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "Database path: " + context.getDatabasePath(DATABASE_NAME).getAbsolutePath());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON");

            // Tạo các bảng
            db.execSQL(CREATE_TABLE_PRODUCTS);
            db.execSQL(CREATE_TABLE_CUSTOMERS);
            db.execSQL(CREATE_TABLE_ORDERS);
            db.execSQL(CREATE_TABLE_ORDER_DETAILS);

            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON");

        // Xóa bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);

        // Tạo lại các bảng
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    // Methods for Products
    public long addProduct(String name, double price, String image, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_NAME, name);
            values.put(COLUMN_PRODUCT_PRICE, price);
            values.put(COLUMN_PRODUCT_IMAGE, image);
            values.put(COLUMN_PRODUCT_DESCRIPTION, description);
            id = db.insert(TABLE_PRODUCTS, null, values);
            Log.d(TAG, "Product added with ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error adding product: " + e.getMessage());
        }

        return id;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product();
                    product.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
                    product.setName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)));
                    product.setPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_PRICE)));
                    product.setImage(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_IMAGE)));
                    product.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_DESCRIPTION)));
                    productList.add(product);
                } while (cursor.moveToNext());
            }
            cursor.close();
            Log.d(TAG, "Retrieved " + productList.size() + " products from database");
        } catch (Exception e) {
            Log.e(TAG, "Error getting products: " + e.getMessage());
        }

        return productList;
    }

    public Product getProductById(long productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;

        try {
            Cursor cursor = db.query(TABLE_PRODUCTS, null,
                    COLUMN_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(productId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                product = new Product();
                product.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_PRICE)));
                product.setImage(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_IMAGE)));
                product.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_DESCRIPTION)));
                Log.d(TAG, "Retrieved product with ID: " + product.getId());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting product by ID: " + e.getMessage());
        }

        return product;
    }

    public boolean updateProduct(long productId, String name, double price, String image, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_NAME, name);
            values.put(COLUMN_PRODUCT_PRICE, price);
            values.put(COLUMN_PRODUCT_IMAGE, image);
            values.put(COLUMN_PRODUCT_DESCRIPTION, description);

            rowsAffected = db.update(TABLE_PRODUCTS, values,
                    COLUMN_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(productId)});
            Log.d(TAG, "Updated product with ID: " + productId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating product: " + e.getMessage());
        }

        return rowsAffected > 0;
    }

    public boolean deleteProduct(long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            // Check if product is used in any order
            Cursor cursor = db.query(TABLE_ORDER_DETAILS, null,
                    COLUMN_ORDER_DETAIL_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(productId)},
                    null, null, null);

            boolean hasOrders = cursor.getCount() > 0;
            cursor.close();

            if (hasOrders) {
                Log.w(TAG, "Cannot delete product with ID: " + productId + " because it's used in orders");
                return false;
            }

            rowsAffected = db.delete(TABLE_PRODUCTS,
                    COLUMN_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(productId)});
            Log.d(TAG, "Deleted product with ID: " + productId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product: " + e.getMessage());
        }

        return rowsAffected > 0;
    }

    // Methods for Customers
    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CUSTOMER_NAME, name);
            values.put(COLUMN_CUSTOMER_PHONE, phone);
            id = db.insert(TABLE_CUSTOMERS, null, values);
            Log.d(TAG, "Customer added with ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error adding customer: " + e.getMessage());
        }

        return id;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customerList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(TABLE_CUSTOMERS, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Customer customer = new Customer();
                    customer.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
                    customer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
                    customer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));
                    customerList.add(customer);
                } while (cursor.moveToNext());
            }
            cursor.close();
            Log.d(TAG, "Retrieved " + customerList.size() + " customers from database");
        } catch (Exception e) {
            Log.e(TAG, "Error getting customers: " + e.getMessage());
        }

        return customerList;
    }

    public Customer getCustomerById(long customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Customer customer = null;

        try {
            Cursor cursor = db.query(TABLE_CUSTOMERS, null,
                    COLUMN_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                customer = new Customer();
                customer.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
                customer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
                customer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));
                Log.d(TAG, "Retrieved customer with ID: " + customer.getId());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting customer by ID: " + e.getMessage());
        }

        return customer;
    }

    public boolean updateCustomer(long customerId, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CUSTOMER_NAME, name);
            values.put(COLUMN_CUSTOMER_PHONE, phone);

            rowsAffected = db.update(TABLE_CUSTOMERS, values,
                    COLUMN_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)});
            Log.d(TAG, "Updated customer with ID: " + customerId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating customer: " + e.getMessage());
        }

        return rowsAffected > 0;
    }

    public boolean deleteCustomer(long customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            // Check if customer has orders
            Cursor cursor = db.query(TABLE_ORDERS, null,
                    COLUMN_ORDER_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)},
                    null, null, null);

            boolean hasOrders = cursor.getCount() > 0;
            cursor.close();

            if (hasOrders) {
                Log.w(TAG, "Cannot delete customer with ID: " + customerId + " because they have orders");
                return false;
            }

            rowsAffected = db.delete(TABLE_CUSTOMERS,
                    COLUMN_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)});
            Log.d(TAG, "Deleted customer with ID: " + customerId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting customer: " + e.getMessage());
        }

        return rowsAffected > 0;
    }

    // Methods for Orders
    public long createOrder(long customerId, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_CUSTOMER_ID, customerId);
            values.put(COLUMN_ORDER_DATE, getCurrentDateTime());
            values.put(COLUMN_ORDER_TOTAL, total);
            id = db.insert(TABLE_ORDERS, null, values);
            Log.d(TAG, "Order created with ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error creating order: " + e.getMessage());
        }

        return id;
    }

    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, c.name as customer_name, c.phone as customer_phone " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "JOIN " + TABLE_CUSTOMERS + " c ON o.customer_id = c.id " +
                    "ORDER BY o.order_date DESC";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Order order = new Order();
                    order.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_ID)));
                    order.setCustomer_id(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_CUSTOMER_ID)));
                    order.setOrder_date(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE)));
                    order.setTotal(cursor.getDouble(cursor.getColumnIndex(COLUMN_ORDER_TOTAL)));
                    order.setCustomer_name(cursor.getString(cursor.getColumnIndex("customer_name")));
                    order.setCustomer_phone(cursor.getString(cursor.getColumnIndex("customer_phone")));

                    // Get order details
                    order.setDetails(getOrderDetailList(order.getId()));

                    orderList.add(order);
                } while (cursor.moveToNext());
            }
            cursor.close();
            Log.d(TAG, "Retrieved " + orderList.size() + " orders from database");
        } catch (Exception e) {
            Log.e(TAG, "Error getting orders: " + e.getMessage());
        }

        return orderList;
    }

    // Method to get order details for a specific order
    private List<Order.OrderDetail> getOrderDetailList(long orderId) {
        List<Order.OrderDetail> detailList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT od.*, p.name as product_name, p.image as product_image, p.description as product_description " +
                    "FROM " + TABLE_ORDER_DETAILS + " od " +
                    "JOIN " + TABLE_PRODUCTS + " p ON od.product_id = p.id " +
                    "WHERE od.order_id = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

            if (cursor.moveToFirst()) {
                do {
                    Order.OrderDetail detail = new Order.OrderDetail();
                    detail.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_DETAIL_ID)));
                    detail.setProduct_id(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_DETAIL_PRODUCT_ID)));
                    detail.setQuantity(cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_DETAIL_QUANTITY)));
                    detail.setPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_ORDER_DETAIL_PRICE)));
                    detail.setProduct_name(cursor.getString(cursor.getColumnIndex("product_name")));
                    detail.setProduct_image(cursor.getString(cursor.getColumnIndex("product_image")));
                    detail.setProduct_description(cursor.getString(cursor.getColumnIndex("product_description")));

                    detailList.add(detail);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting order details: " + e.getMessage());
        }

        return detailList;
    }

    // Methods for Order Details
    public long addOrderDetail(long orderId, long productId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_DETAIL_ORDER_ID, orderId);
            values.put(COLUMN_ORDER_DETAIL_PRODUCT_ID, productId);
            values.put(COLUMN_ORDER_DETAIL_QUANTITY, quantity);
            values.put(COLUMN_ORDER_DETAIL_PRICE, price);
            id = db.insert(TABLE_ORDER_DETAILS, null, values);

            // Update order total
            updateOrderTotal(orderId);
            Log.d(TAG, "Order detail added with ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error adding order detail: " + e.getMessage());
        }

        return id;
    }

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
                    "WHERE o.id = ? " +
                    "LIMIT 1"; // Add LIMIT 1 to get just one result

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
                Log.d(TAG, "Retrieved order with ID: " + order.getId());
            } else {
                Log.w(TAG, "No order found with ID: " + orderId);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting order: " + e.getMessage());
        }

        return order;
    }

    public Order getCompleteOrder(long orderId) {
        Order order = null;
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, c.name as customer_name, c.phone as customer_phone " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "JOIN " + TABLE_CUSTOMERS + " c ON o.customer_id = c.id " +
                    "WHERE o.id = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

            if (cursor.moveToFirst()) {
                order = new Order();
                order.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_ID)));
                order.setCustomer_id(cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_CUSTOMER_ID)));
                order.setOrder_date(cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE)));
                order.setTotal(cursor.getDouble(cursor.getColumnIndex(COLUMN_ORDER_TOTAL)));
                order.setCustomer_name(cursor.getString(cursor.getColumnIndex("customer_name")));
                order.setCustomer_phone(cursor.getString(cursor.getColumnIndex("customer_phone")));

                // Get order details
                order.setDetails(getOrderDetailList(orderId));
                Log.d(TAG, "Retrieved complete order with ID: " + order.getId());
            } else {
                Log.w(TAG, "No order found with ID: " + orderId);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting complete order: " + e.getMessage());
        }

        return order;
    }

    public boolean deleteOrder(long orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // Delete order details first
            int detailsDeleted = db.delete(TABLE_ORDER_DETAILS,
                    COLUMN_ORDER_DETAIL_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
            Log.d(TAG, "Deleted " + detailsDeleted + " order details for order ID: " + orderId);

            // Then delete the order
            int orderDeleted = db.delete(TABLE_ORDERS,
                    COLUMN_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            success = orderDeleted > 0;

            if (success) {
                db.setTransactionSuccessful();
                Log.d(TAG, "Successfully deleted order with ID: " + orderId);
            } else {
                Log.w(TAG, "No order found with ID: " + orderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting order: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public double calculateOrderTotal(long orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        try {
            String query = "SELECT SUM(quantity * price) as total FROM " + TABLE_ORDER_DETAILS +
                    " WHERE " + COLUMN_ORDER_DETAIL_ORDER_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

            if (cursor.moveToFirst()) {
                total = cursor.getDouble(cursor.getColumnIndex("total"));
                Log.d(TAG, "Calculated total for order ID " + orderId + ": " + total);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating order total: " + e.getMessage());
        }

        return total;
    }

    public boolean updateOrderTotal(long orderId) {
        double total = calculateOrderTotal(orderId);
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ORDER_TOTAL, total);

            rowsAffected = db.update(TABLE_ORDERS, values,
                    COLUMN_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
            Log.d(TAG, "Updated total for order ID " + orderId + " to " + total);
        } catch (Exception e) {
            Log.e(TAG, "Error updating order total: " + e.getMessage());
        }

        return rowsAffected > 0;
    }

    // Utility methods
    private String getCurrentDateTime() {
        return dateFormat.format(new Date());
    }

    // Synchronize products from API
    public void syncProductsFromApi(List<Product> apiProducts) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            // Get all local product IDs
            List<Long> localProductIds = new ArrayList<>();
            Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{COLUMN_PRODUCT_ID}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    localProductIds.add(cursor.getLong(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();

            // Process API products
            for (Product apiProduct : apiProducts) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_PRODUCT_NAME, apiProduct.getName());
                values.put(COLUMN_PRODUCT_PRICE, apiProduct.getPrice());
                values.put(COLUMN_PRODUCT_IMAGE, apiProduct.getImage());
                values.put(COLUMN_PRODUCT_DESCRIPTION, apiProduct.getDescription());

                // Check if product exists
                Cursor productCursor = db.query(TABLE_PRODUCTS, new String[]{COLUMN_PRODUCT_ID},
                        COLUMN_PRODUCT_ID + " = ?",
                        new String[]{String.valueOf(apiProduct.getId())},
                        null, null, null);

                if (productCursor.getCount() > 0) {
                    // Update
                    db.update(TABLE_PRODUCTS, values,
                            COLUMN_PRODUCT_ID + " = ?",
                            new String[]{String.valueOf(apiProduct.getId())});
                    localProductIds.remove(Long.valueOf(apiProduct.getId()));
                } else {
                    // Insert
                    values.put(COLUMN_PRODUCT_ID, apiProduct.getId());
                    db.insert(TABLE_PRODUCTS, null, values);
                }
                productCursor.close();
            }

            // Remove products that don't exist in API
            // CAUTION: Uncomment only if you want to delete local products that don't exist in API
            /*
            for (Long localId : localProductIds) {
                // Check if product is used in any order
                Cursor orderDetailCursor = db.query(TABLE_ORDER_DETAILS, null,
                        COLUMN_ORDER_DETAIL_PRODUCT_ID + " = ?",
                        new String[]{String.valueOf(localId)},
                        null, null, null);

                if (orderDetailCursor.getCount() == 0) {
                    // Safe to delete
                    db.delete(TABLE_PRODUCTS,
                            COLUMN_PRODUCT_ID + " = ?",
                            new String[]{String.valueOf(localId)});
                }
                orderDetailCursor.close();
            }
            */

            db.setTransactionSuccessful();
            Log.d(TAG, "Products synchronized with API");
        } catch (Exception e) {
            Log.e(TAG, "Error synchronizing products: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    // Synchronize customers from API
    public void syncCustomersFromApi(List<Customer> apiCustomers) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            // Process API customers
            for (Customer apiCustomer : apiCustomers) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CUSTOMER_NAME, apiCustomer.getName());
                values.put(COLUMN_CUSTOMER_PHONE, apiCustomer.getPhone());

                // Check if customer exists
                Cursor customerCursor = db.query(TABLE_CUSTOMERS, new String[]{COLUMN_CUSTOMER_ID},
                        COLUMN_CUSTOMER_ID + " = ?",
                        new String[]{String.valueOf(apiCustomer.getId())},
                        null, null, null);

                if (customerCursor.getCount() > 0) {
                    // Update
                    db.update(TABLE_CUSTOMERS, values,
                            COLUMN_CUSTOMER_ID + " = ?",
                            new String[]{String.valueOf(apiCustomer.getId())});
                } else {
                    // Insert
                    values.put(COLUMN_CUSTOMER_ID, apiCustomer.getId());
                    db.insert(TABLE_CUSTOMERS, null, values);
                }
                customerCursor.close();
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Customers synchronized with API");
        } catch (Exception e) {
            Log.e(TAG, "Error synchronizing customers: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    // Check database integrity
    public void checkDatabaseIntegrity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA integrity_check", null);

        if (cursor.moveToFirst()) {
            String result = cursor.getString(0);
            if ("ok".equalsIgnoreCase(result)) {
                Log.d(TAG, "Database integrity check passed");
            } else {
                Log.e(TAG, "Database integrity check failed: " + result);
            }
        }
        cursor.close();
    }
}