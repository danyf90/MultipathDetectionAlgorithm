<?php

namespace Vineyard/Utility;

class Notificator {

    //------------------------------
    // Payload data you want to send
    // to Android device (will be
    // accessible via intent extras)
    //------------------------------
    protected $data; // = array( 'mymessage' => 'Hello World!' );

    //------------------------------
    // The recipient registration IDs
    // that will receive the push
    // (Should be stored in your DB)
    //
    // Read about it here:
    // http://developer.android.com/google/gcm/
    //------------------------------
    protected $ids = array(); // array( 'id1' , 'id2' );

    protected const API_KEY = 'AIzaSyCZ9pFQLmaEpTip9y7BCH2wvLgTMKqQS0M';
    protected const GCM_URL = 'https://android.googleapis.com/gcm/send';

    public function __construct(array $data = null, array $ids = array()) {
        $this->setData($data);
        $this->setRecipients($ids);
    }

    public function setData($data) {
        $this->data = $data;
    }

    public function setRecipients($ids) {
        $this->ids = $ids;
    }

    public function send() {

        //------------------------------
        // Set GCM post variables
        // (Device IDs and push payload)
        //------------------------------

        $post = array(
            'registration_ids' => $this->ids,
            'data' => $this->data
        );

        $headers = array(
            'Authorization: key=' . self::API_KEY,
            'Content-Type: application/json'
        );

        // Initialize curl handle
        $ch = curl_init();

        // Set URL to GCM endpoint
        curl_setopt( $ch, CURLOPT_URL, self::GCM_URL );

        // Set request method to POST
        curl_setopt( $ch, CURLOPT_POST, true );

        // Set our custom headers
        curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers );

        // Get the response back as string instead of printing it
        curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

        // Set post data as JSON
        curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $post ) );

        // Actually send the push!
        $response = curl_exec( $ch );

        // Error? Display it!
        if ( curl_errno( $ch ) )
            echo 'GCM error: ' . curl_error( $ch );

        // Close curl handle
        curl_close( $ch );

        // Debug GCM response
        echo $result;
    }
}

?>
