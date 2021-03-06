package app.deyal.deyal_app.repository;

import app.deyal.deyal_app.managers.DataManager;
import app.deyal.deyal_app.data.Register;
import app.deyal.deyal_app.data.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Auth {

    private static final String serverUrl = DataManager.server + "/auth";

    public static boolean login(String email, String password, boolean remember) {
        try {
            //Creating a HttpClient object
            CloseableHttpClient httpclient = HttpClients.createDefault();

            //Creating a HttpPost object
            HttpPost httppost = new HttpPost(serverUrl.concat("/login"));

            //adding email and password
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //Executing the Post request
            HttpResponse httpresponse = httpclient.execute(httppost);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                //getting payload from json
                Scanner scanner = new Scanner(httpresponse.getEntity().getContent());
                String json = scanner.nextLine();
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                String token = jsonObject.getAsJsonObject().get("payload").getAsString();

                DataManager.getInstance().token = token;
                //if remember = true, add preference api to save token
                if (remember) {
                    PreferenceSave.getInstance().setToken(token);
                }
                return true;
            } else {
                //Printing the status line
                System.out.println("inside login");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside login");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean register(Register register) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            HttpPost httppost = new HttpPost(serverUrl.concat("/register"));

            //adding register data
            Gson gson = new Gson();
            String json = gson.toJson(register);
            httppost.setEntity(new StringEntity(json));
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            //Executing the Post request
            HttpResponse httpresponse = httpclient.execute(httppost);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else {
                //Printing the status line
                System.out.println("inside register");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside register");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getUserNameList(String token) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            URIBuilder uriBuilder = new URIBuilder(serverUrl.concat("/list/name"));
            uriBuilder.setParameter("token", token);
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            //Executing the Get request
            HttpResponse httpResponse = httpclient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            System.out.println("Inside get user name");
            if (statusCode == 200) {
                Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
                String json = scanner.nextLine();
                JsonObject userdata = JsonParser.parseString(json).getAsJsonObject().get("payload").getAsJsonObject();

                System.out.println(userdata);
                //Getting user name map from json object
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                DataManager.getInstance().userIdAndNameMap = gson.fromJson(userdata, type);

                System.out.println("Outside get user name");
                return true;
            } else {
                //Printing the status line
                System.out.println(httpResponse.getStatusLine());
                Scanner sc = new Scanner(httpResponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside get user name");
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getUserData(String token) {
        if (token == null) return false;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            //adding token
            URIBuilder uriBuilder = new URIBuilder(serverUrl.concat("/user"));
            uriBuilder.setParameter("token", token);

            //Creating a HttpGet object
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            //Executing the Get request
            HttpResponse httpResponse = httpclient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            System.out.println("Inside get user data");
            if (statusCode == 200) {
                Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
                String json = scanner.nextLine();
                JsonObject userdata = JsonParser.parseString(json).getAsJsonObject().get("payload").getAsJsonObject();

                System.out.println(userdata);

                //Getting user object from json object
                Gson gson = new Gson();
                DataManager.getInstance().userData = gson.fromJson(userdata, User.class);

                System.out.println("Outside get user data");
                return true;
            } else {
                //Printing the status line
                System.out.println(httpResponse.getStatusLine());
                Scanner sc = new Scanner(httpResponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside get user data");
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProfile(String token, User user) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            //adding token
            URIBuilder uriBuilder = new URIBuilder(serverUrl.concat("/update/user"));
            uriBuilder.setParameter("token", token);

            //Creating a HttpPut object
            HttpPut httpPut = new HttpPut(uriBuilder.build());

            //adding user data
            Gson gson = new Gson();
            String json = gson.toJson(user);
            System.out.println(json);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setEntity(new StringEntity(json));

            //Executing the Put request
            HttpResponse httpresponse = httpclient.execute(httpPut);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else {
                //Printing the status line
                System.out.println("inside update user");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside update user");
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changePassword(String token, String newPassword, String oldPassword) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(serverUrl.concat("/update/password"));

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("newPassword", newPassword));
            nameValuePairs.add(new BasicNameValuePair("oldPassword", oldPassword));
            nameValuePairs.add(new BasicNameValuePair("token", token));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //Executing the Post request
            HttpResponse httpresponse = httpclient.execute(httppost);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else {
                //Printing the status line
                System.out.println("inside update password");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside update password");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean searchUser(String token, String userId) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            URIBuilder uriBuilder = new URIBuilder(serverUrl.concat("/search"));
            uriBuilder.setParameter("token", token);
            uriBuilder.setParameter("userId", userId);
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            //Executing the Get request
            HttpResponse httpResponse = httpclient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            System.out.println("inside search user");
            if (statusCode == 200) {
                Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
                String json = scanner.nextLine();
                JsonObject userdata = JsonParser.parseString(json).getAsJsonObject().get("payload").getAsJsonObject();

                System.out.println(userdata);
                Gson gson = new Gson();
                DataManager.getInstance().tempUser = gson.fromJson(userdata, User.class);

                System.out.println("Outside search user");
                return true;
            } else {
                //Printing the status line
                System.out.println(httpResponse.getStatusLine());
                Scanner sc = new Scanner(httpResponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside search user");
                return false;
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendCode(String email) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(serverUrl.concat("/forgot/send"));

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("email", email));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //Executing the Post request
            HttpResponse httpresponse = httpclient.execute(httppost);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else {
                //Printing the status line
                System.out.println("inside send code");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside send code");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyCode(String email, String otp, String password) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(serverUrl.concat("/forgot/verify"));

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("otp", otp));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //Executing the Post request
            HttpResponse httpresponse = httpclient.execute(httppost);

            int statusCode = httpresponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else {
                //Printing the status line
                System.out.println("inside verify code");
                System.out.println(httpresponse.getStatusLine());
                Scanner sc = new Scanner(httpresponse.getEntity().getContent());
                while (sc.hasNext()) {
                    System.out.println(sc.nextLine());
                }
                System.out.println("Outside verify code");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
