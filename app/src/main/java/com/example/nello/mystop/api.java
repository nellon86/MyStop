package com.example.nello.mystop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import com.google.gson.Gson;

import com.example.nello.mystop.*;

public class api {

    static final String BASE_URL = "https://openbus.emtmadrid.es/emt-proxy-server/last";
    static final String TIMES_FROM_STOP_URL = "/geo/GetArriveStop.php";
    static String API_CLIENT_ID;
    static String API_PASSKEY;
    private final static Logger log = Logger.getLogger(api.class.getName());

    /* Returns bus times from stop if 200, null otherwise */
    public static busList getTimesFromStop(int stopCode) {


        try {

			/*Building the client*/
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(BASE_URL + TIMES_FROM_STOP_URL);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("idClient", API_CLIENT_ID));
            urlParameters.add(new BasicNameValuePair("passKey", API_PASSKEY));
            urlParameters.add(new BasicNameValuePair("cultureInfo", "ES"));
            urlParameters.add(new BasicNameValuePair("idStop", Integer.toString(stopCode)));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

			/*Parse the response*/
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

			/*Convert to JSON*/
            final Gson gson = new Gson();
            final busList stopList = gson.fromJson(result.toString(),
                    busList.class);

            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("Status 200, returning IncomingBuses");
                return stopList;
            } else {
                log.warning("Status" + response.getStatusLine().getStatusCode() +  ", returning null");
                return null;
            }

        } catch (IOException e){
            log.severe("Error while connecting to API, returning null");
            e.printStackTrace();
            return null;

        }
        catch (Exception e) {
            log.severe("Unknown error, returning null");
            e.printStackTrace();
            return null;
        }

    }

    public static busList getTimesFromStopSpecificLine(int stopCode,
                                                               int lineNumber) {
        busList original = getTimesFromStop(stopCode);
        busList filtered = new busList();
        incomingBus currentBus;

        for (int i = 0; i < original.getArrives().size(); i++) {
            currentBus = original.getArrives().get(i);
            if (currentBus.getLineId() == lineNumber) {
                filtered.addIncomingBus(currentBus);
            }
        }
        return filtered;
    }

    public static void main(String[] args) {
		/*Log to console*/
        log.setLevel(Level.WARNING);

        busList incomingBusList;

        switch (args.length) {
			/*This case pretty console returns all bus lines for a bus stop*/
			/*5 arguments expected: CLIENT_ID, PASSKEY, incomingBusStop, console-pretty, busStop*/
            case 5: {
                if ((args[2].equals("incomingBusToStop"))&&(args[3].equals("pretty-console"))) {
                    try {
                        API_CLIENT_ID = args[0];
                        API_PASSKEY = args[1];
                        int busStop = Integer.parseInt(args[4]);

                        incomingBusList = api.getTimesFromStop(busStop);
                        helper.prettyPrintToConsole(incomingBusList);
                        break;

                    } catch (NumberFormatException e) {
                        log.severe("STOP_NUMBER format incorrect");
                        log.severe(e.toString());
                        helper.printUsageDirectives();
                        break;
                    }
                } else {
                    log.severe("Unrecognized action");
                    helper.printUsageDirectives();
                    break;
                }
            }
			/*This case pretty console returns bus for a given line in a given bus stop*/
			/*6 arguments expected: CLIENT_ID, PASSKEY, incomingBusStop,console-pretty, busStop, busLine*/
            case 6: {
                if ((args[2].equals("incomingBusToStop"))&&(args[3].equals("pretty-console"))) {
                    try {
                        API_CLIENT_ID = args[0];
                        API_PASSKEY = args[1];
                        int busStop = Integer.parseInt(args[4]);
                        int busLine = Integer.parseInt(args[5]);

                        incomingBusList = api.getTimesFromStopSpecificLine(busStop,
                                busLine);
                        helper.prettyPrintToConsole(incomingBusList);
                        break;

                    } catch (NumberFormatException e) {
                        log.severe("STOP_NUMBER or BUS_LINE format incorrect");
                        log.severe(e.toString());
                        helper.printUsageDirectives();
                        break;
                    }
                } else {
                    log.severe("Unrecognized action");
                    helper.printUsageDirectives();
                    break;
                }
            }
			/*This case returns the seconds for a given instance(first or second bus) for a given line
			 * in a given bus stop
			/* 7 arguments expected: CLIENT_ID, PASSKEY, incomingBusStop,bare-seconds,
			 * busStop, busLine, instance */
            case 7: {
                if ((args[2].equals("incomingBusToStop"))&&(args[3].equals("bare-seconds"))) {
                    try {
                        API_CLIENT_ID = args[0];
                        API_PASSKEY = args[1];
                        int busStop = Integer.parseInt(args[4]);
                        int busLine = Integer.parseInt(args[5]);
                        int instance = Integer.parseInt(args[6]);

                        incomingBusList = api.getTimesFromStopSpecificLine(busStop,
                                busLine);
                        helper.printSecondsToInstance(incomingBusList, instance);
                        break;

                    } catch (NumberFormatException e) {
                        log.severe("STOP_NUMBER or BUS_LINE format incorrect");
                        log.severe(e.toString());
                        helper.printUsageDirectives();
                        break;
                    }
                } else {
                    log.severe("Unrecognized action");
                    helper.printUsageDirectives();
                    break;
                }
            }
            default: {
                helper.printUsageDirectives();
                break;
            }

        }
    }
}
