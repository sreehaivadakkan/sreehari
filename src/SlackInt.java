import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;

/**
 * Created by sreehari.v on 09/06/15.
 * 
 */
public class SlackInt extends AnAction {

    private String authenticationToken;
    private String crucibleId;

    private void authenticate(final AnActionEvent e) {
        final File FILE = new File("cruccomment_authenticates.txt");
        if (!FILE.exists()) {
            final JFrame NEW_FRAME0 = new JFrame("Registration");
            final JPanel SAMPLE_PANEL = new JPanel();

            SAMPLE_PANEL.setPreferredSize(new Dimension(300, 200));
            SAMPLE_PANEL.setMaximumSize(new Dimension(300, 200));

            JLabel msg = new JLabel("You are not registered.Please register.");
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg1 = new JLabel("Enter Crucible Username");
            msg1.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg2 = new JLabel("Enter Crucible Password");
            msg2.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JTextField SAMPLE_TEXT_FIELD = new JTextField();
            SAMPLE_TEXT_FIELD.setMaximumSize(new Dimension(300, 30));
            SAMPLE_TEXT_FIELD.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JTextField SAMPLE_TEXT_FIELD1 = new JTextField();
            SAMPLE_TEXT_FIELD1.setMaximumSize(new Dimension(300, 30));
            SAMPLE_TEXT_FIELD1.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton jComment1 = new JButton();
            jComment1.setText("Register");
            jComment1.setAlignmentX(Component.LEFT_ALIGNMENT);

            ActionListener actionListener0 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String user = SAMPLE_TEXT_FIELD.getText();
                    String pass = SAMPLE_TEXT_FIELD1.getText();
                    HttpClient httpClient0 = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet("http://localhost:8060/rest-service/auth-v1/login.json?userName="+user+"&password="+pass);
                    HttpResponse response = null;
                    try {
                        response = httpClient0.execute(request);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String jsonString0 = null;
                    try {
                        jsonString0 = EntityUtils.toString(response.getEntity());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println(jsonString0);
                    try {
                        JSONObject jsonResult0 = new JSONObject(jsonString0);
                        String authToken = jsonResult0.get("token").toString();
                        FILE.createNewFile();
                        FileWriter fw = null;
                        try {
                            fw = new FileWriter(FILE.getAbsoluteFile());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(authToken);
                        authenticationToken=authToken;
                        try {
                            bw.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(SAMPLE_PANEL, "You are registered");
                        NEW_FRAME0.setVisible(false);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    httpClient0.getConnectionManager().shutdown();

                }
            };

            jComment1.addActionListener(actionListener0);

            SAMPLE_PANEL.setLayout(new BoxLayout(SAMPLE_PANEL, 3));
            SAMPLE_PANEL.add(msg);
            SAMPLE_PANEL.add(msg1);
            SAMPLE_PANEL.add(SAMPLE_TEXT_FIELD);
            SAMPLE_PANEL.add(msg2);
            SAMPLE_PANEL.add(SAMPLE_TEXT_FIELD1);
            SAMPLE_PANEL.add(jComment1);
            NEW_FRAME0.setContentPane(SAMPLE_PANEL);
            NEW_FRAME0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            NEW_FRAME0.pack();
            NEW_FRAME0.setVisible(true);
        }
        else {
            FileReader fw = null;
            try {
                fw = new FileReader(FILE.getAbsoluteFile());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            BufferedReader rw = new BufferedReader(fw);
            try {
                authenticationToken = rw.readLine();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                rw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void createCrucibleReview(final String auth, final JFrame frame, AnActionEvent e) throws IOException, JSONException {

        final JFrame NEW_FRAME0 = new JFrame("Create Crucible Review");

        final JPanel SAMPLE_PANEL = new JPanel();

        SAMPLE_PANEL.setPreferredSize(new Dimension(200,400));
        SAMPLE_PANEL.setMaximumSize(new Dimension(200, 400));

        JLabel msg = new JLabel("Project Key");
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg1 = new JLabel("Review Name");
        msg1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg2 = new JLabel("Description");
        msg2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg3 = new JLabel("Author");
        msg3.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg4 = new JLabel("Moderator");
        msg4.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg5 = new JLabel("Due date");
        msg5.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg6 = new JLabel("Commit ID");
        msg6.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg8 = new JLabel("Repository");
        msg8.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField REVIEW_NAME = new JTextField();
        REVIEW_NAME.setMaximumSize(new Dimension(300, 30));
        REVIEW_NAME.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField DES = new JTextField();
        DES.setMaximumSize(new Dimension(300, 30));
        DES.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField DUE_DATE = new JTextField();
        DUE_DATE.setMaximumSize(new Dimension(300, 30));
        DUE_DATE.setAlignmentX(Component.LEFT_ALIGNMENT);
        DUE_DATE.setText("2015-06-19T16:50");

        final JTextField COMMIT_ID = new JTextField();
        COMMIT_ID.setMaximumSize(new Dimension(300, 30));
        COMMIT_ID.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton jComment1= new JButton();
        jComment1.setText("Create Review");
        jComment1.setAlignmentX(Component.LEFT_ALIGNMENT);

        final HttpClient HTTPCLIENT1 = HttpClientBuilder.create().build();
        HttpGet request1 = new HttpGet("http://localhost:8060/rest-service/projects-v1.json?FEAUTH=" + auth);
        request1.addHeader("content-type", "application/json");
        HttpResponse response1;
        String jsonString;
        response1 = HTTPCLIENT1.execute(request1);
        jsonString = EntityUtils.toString(response1.getEntity());
        JSONObject jsonResult = new JSONObject(jsonString);
        System.out.println(jsonString);
        final JSONArray DATA1 = jsonResult.getJSONArray("projectData");
        final String[] PROJECT_ID = new String[DATA1.length()];
        final String[] PROJECT_KEY = new String[DATA1.length()];
        for (int i = 0; i < DATA1.length(); i++) {
            PROJECT_ID[i] = DATA1.getJSONObject(i).getString("name");
            PROJECT_KEY[i] = DATA1.getJSONObject(i).getString("key");
        }

        final JComboBox comboBoxProject=new JComboBox(PROJECT_ID);
        comboBoxProject.setMaximumSize(new Dimension(300, 30));
        comboBoxProject.setAlignmentX(Component.LEFT_ALIGNMENT);


        final HttpClient HTTPCLIENT2 = HttpClientBuilder.create().build();
        HttpGet request2 = new HttpGet("http://localhost:8060/rest-service/users-v1.json?FEAUTH=" + auth);
        request2.addHeader("content-type", "application/json");
        HttpResponse response2 = null;
        String jsonString2;
        response2 = HTTPCLIENT2.execute(request2);
        jsonString2 = EntityUtils.toString(response2.getEntity());
        JSONObject jsonResult2 = new JSONObject(jsonString2);
        final JSONArray DATA2 = jsonResult2.getJSONArray("userData");
        final String[] AUTHOR = new String[DATA2.length()];
        for (int i = 0; i < DATA2.length(); i++) {
            AUTHOR[i] = DATA2.getJSONObject(i).getString("userName");
        }



        final HttpClient HTTPCLIENT4 = HttpClientBuilder.create().build();
        HttpGet request4 = new HttpGet("http://localhost:8060/rest-service/repositories-v1.json?FEAUTH=" + auth);
        request4.addHeader("content-type", "application/json");
        HttpResponse response4 = null;
        String jsonString4;
        response4 = HTTPCLIENT4.execute(request4);
        jsonString4 = EntityUtils.toString(response4.getEntity());
        JSONObject jsonResult4 = new JSONObject(jsonString4);
        final JSONArray DATA4 = jsonResult4.getJSONArray("repoData");
        final String[] REPO = new String[DATA4.length()];
        for (int i = 0; i < DATA4.length(); i++) {
            REPO[i] = DATA4.getJSONObject(i).getString("name");
        }

        final JComboBox comboBoxRepo=new JComboBox(REPO);
        comboBoxRepo.setMaximumSize(new Dimension(300, 30));
        comboBoxRepo.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Checkbox[] checkboxes = new Checkbox[AUTHOR.length];

        final JComboBox comboBoxAuthor=new JComboBox(AUTHOR);
        comboBoxAuthor.setMaximumSize(new Dimension(300, 30));
        comboBoxAuthor.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JComboBox comboBoxModerator=new JComboBox(AUTHOR);
        comboBoxModerator.setMaximumSize(new Dimension(300, 30));
        comboBoxModerator.setAlignmentX(Component.LEFT_ALIGNMENT);

        ActionListener actionListener0=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                HttpClient httpClient3 = HttpClientBuilder.create().build();
                HttpPost request3 = new HttpPost("http://localhost:8060/rest-service/reviews-v1.json?FEAUTH="+auth);
                StringEntity params3 =null;
                try {
                    params3 = new StringEntity("{\"reviewData\" : {" +
                            "    \"projectKey\" : \""+PROJECT_KEY[comboBoxProject.getSelectedIndex()]+"\"," +
                            "    \"name\" : \""+REVIEW_NAME.getText()+"\"," +
                            "    \"description\" : \""+DES.getText()+"\"," +
                            "    \"author\" : {" +
                            "      \"userName\" : \""+comboBoxAuthor.getSelectedItem().toString()+"\"" +
                            "    }," +
                            "    \"moderator\" : {" +
                            "      \"userName\" : \""+comboBoxModerator.getSelectedItem().toString()+"\"" +
                            "    }," +
                            "    \"creator\" : {" +
                            "      \"userName\" : \""+comboBoxAuthor.getSelectedItem().toString()+"\"" +
                            "    }," +
                            "    \"type\" : \"REVIEW\"," +
                            "    \"allowReviewersToJoin\" : true," +
                            "    \"metricsVersion\" : 1," +
                            "    \"createDate\" : \"2015-06-9T16:50:15.365+0200\"," +
                            "    \"dueDate\" : \""+DUE_DATE.getText()+":15.365+0200"+"\"" +
                            "  }," +
                            "  \"changesets\" : {" +
                            "    \"changesetData\" : [ {" +
                            "      \"id\" : \""+COMMIT_ID.getText()+"\"" +
                            "    } ]," +
                            "    \"repository\" : \""+comboBoxRepo.getSelectedItem().toString()+"\"" +
                            "  }" +
                            "}");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                request3.setEntity(params3);
                request3.addHeader("content-type", "application/json");
                HttpResponse response3 = null;
                try {
                    response3 = httpClient3.execute(request3);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String jsonString3 = null;
                try {
                    jsonString3 = EntityUtils.toString(response3.getEntity());
                    JSONObject jsonResult3 = new JSONObject(jsonString3);
                    crucibleId=jsonResult3.getJSONObject("permaId").getString("id");
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HttpClient httpClient5 = HttpClientBuilder.create().build();
                HttpPost request5 = new HttpPost("http://localhost:8060/rest-service/reviews-v1/"+crucibleId+"/reviewers.json?FEAUTH=user1:13:3fe69b0f589b0f770727015e5051e134");
                StringEntity params5 =null;
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i=0;i<AUTHOR.length;i++) {
                        if (checkboxes[i].getState()==true){
                            if(stringBuilder.length()==0) {
                                stringBuilder.append(AUTHOR[i]);
                            }
                            else
                                stringBuilder.append(","+AUTHOR[i]);
                        }
                    }
                    params5 = new StringEntity(stringBuilder.toString());
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                request5.setEntity(params5);
                request5.addHeader("content-type", "application/json");
                try {
                    httpClient5.execute(request5);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                NEW_FRAME0.setVisible(false);
                frame.setVisible(true);
            }
        };

        jComment1.addActionListener(actionListener0);
        SAMPLE_PANEL.setLayout(new BoxLayout(SAMPLE_PANEL, 3));
        SAMPLE_PANEL.add(msg);
        SAMPLE_PANEL.add(comboBoxProject);
        SAMPLE_PANEL.add(msg1);
        SAMPLE_PANEL.add(REVIEW_NAME);
        SAMPLE_PANEL.add(msg2);
        SAMPLE_PANEL.add(DES);
        SAMPLE_PANEL.add(msg3);
        SAMPLE_PANEL.add(comboBoxAuthor);
        SAMPLE_PANEL.add(msg4);
        SAMPLE_PANEL.add(comboBoxModerator);
        SAMPLE_PANEL.add(msg5);
        SAMPLE_PANEL.add(DUE_DATE);
        SAMPLE_PANEL.add(msg6);
        SAMPLE_PANEL.add(COMMIT_ID);
        SAMPLE_PANEL.add(msg8);
        SAMPLE_PANEL.add(comboBoxRepo);


        SAMPLE_PANEL.add(jComment1);

        final JPanel SAMPLE_PANEL2 = new JPanel();
        SAMPLE_PANEL2.setPreferredSize(new Dimension(100, 100));
        SAMPLE_PANEL2.setMaximumSize(new Dimension(100, 100));
        SAMPLE_PANEL2.setLayout(new BoxLayout(SAMPLE_PANEL2, 3));
        JLabel msg7 = new JLabel("Reviewers");
        msg7.setAlignmentX(Component.RIGHT_ALIGNMENT);
        SAMPLE_PANEL2.add(msg7);
        for(int i=0;i<AUTHOR.length;i++) {
            checkboxes[i]= new Checkbox(AUTHOR[i]);
            SAMPLE_PANEL2.add(checkboxes[i]);
        }


        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(300, 400));
        contentPane.setLayout(new BoxLayout(contentPane, 2));
        contentPane.add(SAMPLE_PANEL);
        contentPane.add(SAMPLE_PANEL2);


        NEW_FRAME0.setContentPane(contentPane);
        NEW_FRAME0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        NEW_FRAME0.pack();
        NEW_FRAME0.setVisible(true);
    }

    public void actionPerformed(AnActionEvent e) {

        authenticate(e);
        final File FILE = new File("authentication_tests.txt");
        if (!FILE.exists()) {
            final JFrame NEW_FRAME0 = new JFrame("Registration");
            final JPanel SAMPLE_PANEL = new JPanel();

            SAMPLE_PANEL.setPreferredSize(new Dimension(300,100));
            SAMPLE_PANEL.setMaximumSize(new Dimension(300, 100));

            JLabel msg = new JLabel("You are not registered.Please register.");
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg1 = new JLabel("Enter Token");
            msg1.setAlignmentX(Component.LEFT_ALIGNMENT);

            final JTextField SAMPLE_TEXT_FIELD = new JTextField();
            SAMPLE_TEXT_FIELD.setMaximumSize(new Dimension(300, 30));
            SAMPLE_TEXT_FIELD.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton jComment1= new JButton();
            jComment1.setText("Register");
            jComment1.setAlignmentX(Component.LEFT_ALIGNMENT);

            ActionListener actionListener0=new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String tok= SAMPLE_TEXT_FIELD.getText();
                    HttpClient httpClient0 = HttpClientBuilder.create().build();
                    HttpPost request = new HttpPost("https://slack.com/api/auth.test");
                    StringEntity params = null;
                    try {
                        params = new StringEntity("token="+tok);
                    }
                    catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    request.addHeader("content-type", "application/x-www-form-urlencoded");
                    request.setEntity(params);
                    HttpResponse response = null;
                    try {
                        response = httpClient0.execute(request);
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String jsonString0 = null;
                    try {
                        jsonString0 = EntityUtils.toString(response.getEntity());
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println(jsonString0);
                    try {
                        JSONObject jsonResult0 = new JSONObject(jsonString0);
                        String tempRes=jsonResult0.get("ok").toString();
                        if(tempRes.equals("false")) {
                            String error=jsonResult0.get("error").toString();
                            if(error.equals("invalid_auth"))
                                JOptionPane.showMessageDialog(SAMPLE_PANEL, "Invalid authentication token provided.");
                            else if (error.equals("account_inactive"))
                                JOptionPane.showMessageDialog(SAMPLE_PANEL, "Authentication token is for a deleted user.");
                        }
                        else {
                            FILE.createNewFile();
                            FileWriter fw = null;
                            try {
                                fw = new FileWriter(FILE.getAbsoluteFile());
                            }
                            catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(tok);
                            try {
                                bw.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            JOptionPane.showMessageDialog(SAMPLE_PANEL, "You are registered. Now you can request for Review.");
                            NEW_FRAME0.setVisible(false);
                            System.out.println("Done");
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    httpClient0.getConnectionManager().shutdown();
                }
            };
            jComment1.addActionListener(actionListener0);
            SAMPLE_PANEL.setLayout(new BoxLayout(SAMPLE_PANEL,3));
            SAMPLE_PANEL.add(msg);
            SAMPLE_PANEL.add(msg1);
            SAMPLE_PANEL.add(SAMPLE_TEXT_FIELD);
            SAMPLE_PANEL.add(jComment1);
            NEW_FRAME0.setContentPane(SAMPLE_PANEL);
            NEW_FRAME0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            NEW_FRAME0.pack();
            try {
                createCrucibleReview(authenticationToken,NEW_FRAME0,e);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        {
            String newToken =null;
            FileReader fw = null;
            try {
                fw = new FileReader(FILE.getAbsoluteFile());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            BufferedReader rw = new BufferedReader(fw);
            try {
                newToken = rw.readLine();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                rw.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("Done");
            HttpClient httpClient = HttpClientBuilder.create().build();
            try {
                HttpPost request = new HttpPost("https://slack.com/api/users.list");
                StringEntity params = new StringEntity("token="+newToken);
                request.addHeader("content-type", "application/x-www-form-urlencoded");
                request.setEntity(params);
                HttpResponse response = httpClient.execute(request);
                String jsonString = EntityUtils.toString(response.getEntity());
                JSONObject jsonResult = new JSONObject(jsonString);
                final JSONArray data = jsonResult.getJSONArray("members");
                if (data != null) {
                    final String[] names = new String[data.length()];
                    final String[] id = new String[data.length()];
                    String[] deleted = new String[data.length()];
                    final Boolean[] selected = new Boolean[data.length()];
                    for (int i = 0; i < data.length(); i++) {
                        names[i] = data.getJSONObject(i).getString("name");
                        id[i]=data.getJSONObject(i).getString("id");
                        deleted[i] = data.getJSONObject(i).get("deleted").toString();
                    }
                    final JPanel jPanel = new JPanel();
                    jPanel.setBackground(Color.gray);
                    Dimension dimension = new Dimension();
                    dimension.setSize(200, data.length() * 20);
                    jPanel.setPreferredSize(dimension);
                    jPanel.setLayout(new BoxLayout(jPanel, 3));
                    final Checkbox[] checkboxes = new Checkbox[data.length()];
                    for(int i=0;i<data.length();i++) {
                        checkboxes[i]= new Checkbox(names[i]);
                        jPanel.add(checkboxes[i]);
                    }

                    JTextField textPane=new JTextField(newToken);
                    textPane.setMaximumSize(new Dimension(200, 30));
                    textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    JLabel label=new JLabel();
                    label.setText("Registered Token");
                    label.setMaximumSize(new Dimension(200, 30));
                    label.setAlignmentX(Component.LEFT_ALIGNMENT);
                    JLabel label1=new JLabel();
                    label1.setText("Message for reviewers");
                    label1.setMaximumSize(new Dimension(200, 30));
                    label1.setAlignmentX(Component.LEFT_ALIGNMENT);
                    JLabel label2=new JLabel();
                    label2.setText("Repository Link");
                    label2.setMaximumSize(new Dimension(200, 30));
                    label2.setAlignmentX(Component.LEFT_ALIGNMENT);

                    final JTextField textrepo=new JTextField();
                    textrepo.setMaximumSize(new Dimension(200, 30));
                    textrepo.setAlignmentX(Component.LEFT_ALIGNMENT);

                    final JEditorPane editorPane=new JEditorPane();
                    editorPane.setMaximumSize(new Dimension(200, 100));
                    editorPane.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JButton jComment= new JButton();
                    jComment.setAlignmentX(Component.LEFT_ALIGNMENT);
                    jComment.setText("Send for review");
                    jComment.setForeground(Color.BLUE);
                    jComment.setBounds(0, 30, 0, 30);
                    final String finalNewToken = newToken;
                    final JFrame frame = new JFrame("Reviewers");

                    ActionListener actionListener=new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            for (int i = 0; i < data.length(); i++) {
                                selected[i] = (checkboxes[i].getState());
                            }
                            for (int i = 0; i < data.length(); i++) {
                                if(selected[i]) {
                                    HttpClient httpClients = HttpClientBuilder.create().build();
                                    HttpPost request = new HttpPost("https://slack.com/api/chat.postMessage");
                                    StringEntity params = null;
                                    try {
                                        params = new StringEntity("username=Code_Review_Bot&token="+ finalNewToken +"&channel="+id[i]+"&text=You have got a review request\nMessage: "+editorPane.getText()+"\nRepository Link: "+textrepo.getText()+"\nReview ID: "+crucibleId);
                                    } catch (UnsupportedEncodingException e1) {
                                        e1.printStackTrace();
                                    }
                                    request.addHeader("content-type", "application/x-www-form-urlencoded");
                                    request.setEntity(params);
                                    try {
                                        HttpResponse response = httpClients.execute(request);
                                        String jsonString = EntityUtils.toString(response.getEntity());
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                    httpClients.getConnectionManager().shutdown();
                                }
                            }
                            frame.setVisible(false);
                        }
                    };
                    jComment.addActionListener(actionListener);

                    JPanel left = new JPanel();
                    left.setPreferredSize(new Dimension(200, 300));
                    left.setLayout(new BoxLayout(left, 3));
                    left.add(label);
                    left.add(textPane);
                    left.add(label2);
                    left.add(textrepo);
                    left.add(label1);
                    left.add(editorPane);
                    left.add(jComment);

                    JScrollPane scrollPane = new JScrollPane(jPanel);
                    Dimension newD= new Dimension();

                    scrollPane.setPreferredSize(newD);
                    scrollPane.createVerticalScrollBar();
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

                    JPanel contentPane = new JPanel();
                    contentPane.setPreferredSize(new Dimension(450, 300));
                    contentPane.setLayout(new BoxLayout(contentPane, 2));
                    contentPane.add(left);
                    contentPane.add(scrollPane);

                    frame.setContentPane(contentPane);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    createCrucibleReview(authenticationToken,frame,e);
                }
            }catch (Exception ex) {
                System.out.println(ex.getMessage());
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
}
