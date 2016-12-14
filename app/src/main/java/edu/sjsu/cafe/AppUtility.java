package edu.sjsu.cafe;

import android.content.Context;
import android.database.DatabaseUtils;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.sjsu.cafe.offers.OfferManager;
import edu.sjsu.cafe.menu.MenuSection;

/**
 * Created by Akshay on 12/5/2016.
 */

public class AppUtility {

    public static int offerCount = 0;

    private static HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> restaurantMenu = new HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>();
    public static ArrayList<String> restaurantMenuItems = new ArrayList<String>();
    private static ArrayList<HashMap<String, ArrayList<String>>> subMenu = new ArrayList<HashMap<String, ArrayList<String>>>();
    private static ArrayList<String> items = new ArrayList<String>();
    private static String mainMenuItem, subMenuItem, menuItem;
    private static HashMap<String, ArrayList<String>> subMenuElem;
    private static Boolean atleastOneMenuItemExists = false;

    public static HashMap<String, String> menuImages = new HashMap<String, String>();
    public static List<MenuSection> menuSectionsList;

    public static final int PERMISSIONS_REQUEST_PHONE_CALL = 100;

    final static String restaurantMenuFileName = "menu.txt";

    public static boolean isNullOrEmpty(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String escapeHTML(String str) {
        if (!isNullOrEmpty(str)) {
            str = str.replaceAll("<(.*?)\\>", " ");//Removes all items in brackets
            str = str.replaceAll("<(.*?)\\\n", " ");//Must be undeneath
            str = str.replaceFirst("(.*?)\\>", " ");//Removes any connected item to the last bracket
            str = str.replaceAll("&nbsp;", " ");
            str = str.replaceAll("&amp;", " ");
            str = str.trim();
        } else {
            str = "";
        }
        return str;
    }


    public static void readRestaurantMenu(BufferedReader bufferedReader) {
        if (restaurantMenu == null || restaurantMenu.isEmpty()) {
            //read the file
            try {
                String line = "";
                MenuSection menuSection;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!isNullOrEmpty(line)) {
                        line = line.trim();
                        if (line.startsWith("*")) {
                            if (!isNullOrEmpty(mainMenuItem) && restaurantMenu.containsKey(mainMenuItem)) {
                                if (items != null && !items.isEmpty()) {
                                    subMenuElem.put(subMenuItem, items);
                                    subMenu.add(subMenuElem);
                                    restaurantMenu.put(mainMenuItem, subMenu);
                                }
                            }
                            mainMenuItem = line.replace("*", "").trim();
                            restaurantMenu.put(mainMenuItem, null);
                            restaurantMenuItems.add(mainMenuItem);
                            items = new ArrayList<String>();
                            subMenuItem = "";
                            subMenuElem = new HashMap<String, ArrayList<String>>();
                            subMenu = new ArrayList<HashMap<String, ArrayList<String>>>();

                        } else if (line.startsWith("##")) {
                            if (!isNullOrEmpty(subMenuItem) && items != null && !items.isEmpty()) {
                                subMenuElem.put(subMenuItem, items);
                                subMenu.add(subMenuElem);
                                restaurantMenu.put(mainMenuItem, subMenu);
                            }

                            items = new ArrayList<String>();
                            subMenuItem = line.replace("##", "").trim();
                            subMenuElem = new HashMap<String, ArrayList<String>>();

                        } else if (line.startsWith("---")) {
                            atleastOneMenuItemExists = true;
                            menuItem = line.replace("---", "").trim();
                            items.add(menuItem);
                        }
                    }
                }

                /*
                process last block of menu <- this won't be processed in the while loop because
                the loop exits after not being able to find another line
                 */
                if (atleastOneMenuItemExists) {
                    subMenuElem.put(subMenuItem, items);
                    subMenu.add(subMenuElem);
                    restaurantMenu.put(mainMenuItem, subMenu);
                }

            } catch (Exception ex) {

            }
        }
    }

    public static List<MenuSection> getMenuSections() {
        return menuSectionsList;
    }

    public static ArrayList<HashMap<String, ArrayList<String>>> getMenuItems(String menuSection) {
        return restaurantMenu.get(menuSection);
    }

    public static void oneTimeDownloadOffer(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String offerExpiry = sdf.format(new Date());
        int offerLength = Integer.parseInt(context.getString(R.string.first_time_offer_validity_days));
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(offerExpiry));
            c.add(Calendar.DATE, offerLength);  // set default offer expiry date
            offerExpiry = sdf.format(c.getTime());

            OfferManager offerManager = new OfferManager(context);
            offerManager.open();
            offerManager.insertOffer(context.getString(R.string.first_time_offer_code), context.getString(R.string.first_time_offer_description), offerExpiry);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String trimText(String str, int length) {
        if (!isNullOrEmpty(str)) {
            if (str.length() > length) {
                str = str.substring(0, length - 1);
                str += "...";
            }
        }
        return str;
    }

    public static String getMenuSectionImageUri(String menuItem) {
        menuItem = menuItem.toLowerCase();
        menuItem = menuItem.replace(" ", "_");
        if (menuImages != null && !menuImages.isEmpty() && menuImages.containsKey(menuItem)) {
            return menuImages.get(menuItem);
        }
        return "";
    }

    public static void fetchMenuImageResource(Context context) {
        try {
            String[] images = context.getResources().getAssets().list("food");
            for (String imageName : images) {
                menuImages.put(imageName.split("\\.")[0], "file:///android_asset/food/" + imageName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        populateMenuSections();
    }

    public static void populateMenuSections() {
        menuSectionsList = new ArrayList<MenuSection>();
        MenuSection menuSection;
        for (String sectionName : restaurantMenuItems) {
            if (!isNullOrEmpty(getMenuSectionImageUri(sectionName))) {
                menuSection = new MenuSection(sectionName, getMenuSectionImageUri(sectionName));
            } else {
                menuSection = new MenuSection(sectionName, null);
            }
            menuSectionsList.add(menuSection);
        }
    }

    public static String sanitizeString(String str) {
        if (!isNullOrEmpty(str)) {
            str = DatabaseUtils.sqlEscapeString(str);
            str = str.replaceAll("'", "");
            str = str.replaceAll("\"", "");
        }
        return str;

    }
}
