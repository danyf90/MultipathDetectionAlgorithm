<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;

class Photo implements IResource {
    
    const PHOTO_DIRECTORY = "Photo/";
    const THUMB_DIRECTORY = static::PHOTO_DIRECTORY . "Thumb/";

    public static function handleRequest($method, array $requestParameters) {
        
        if ( $method != "GET" ) {
            http_response_code(501); // Not Implemented;
            return;
        }
        
        if ( count($requestParameters) != "1" ) {
            http_response_code(400); // Bad Request;
            return;
        }
        
        $filename = array_shift($requestParameters);
        
        if (!static::exists($filename))  {
            http_response_code(404); // Not Found
            return;
        }
        
        $filename = static::getThumbnailPath($filename);
        
        static::echoPhotoData($filename);        
    }
    
    public static function getFullPhotoPath($filename) {
        return static::PHOTO_DIRECTORY . $filename;
    }
    
    protected static function exists($filename) {
        return file_exists(static::PHOTO_DIRECTORY . $filename);
    }
    
    public static function getThumbnailPath($filename) {
        $w = (isset($_GET['w'])) ? $_GET['w'] : 0;
        $h = (isset($_GET['h'])) ? $_GET['h'] : 0;
        
        // original image
        if ($w == 0 && $h == 0)
            return $filename;
        
        if ($w == 0) // scale to width w
            $path = static::THUMB_DIRECTORY . "wx" . $h . "_" . $filename;
        else if ($h == 0) // scale to height h
            $path = static::THUMB_DIRECTORY . $w . "xh_" . $filename;
        else
            $path = static::THUMB_DIRECTORY . $w . "x" . $h . "_" . $filename;
    
        if (!file_exists(static::THUMB_DIRECTORY))
            static::generateThumb($filename, $w, $h);
        
        return $path;
    }
    
    protected static function generateThumb($filename, $desiredW, $desiredH) {
        
        // Attempt to open file
        $filepath = static::getFullPhotoPath($filename);
        $type = exif_imagetype($filepath);
        
        switch ($type) { 
            case 1 : // gif
                $originalImage = imageCreateFromGif($filepath); 
            break; 
            case 2 : // jpeg
                $originalImage = imageCreateFromJpeg($filepath); 
            break; 
            case 3 : // png
                $originalImage = imageCreateFromPng($filepath); 
            break; 
            case 6 : // bmp
                $originalImage = imageCreateFromBmp($filepath); 
            break; 
            default:
                // TODO generate black image as "unsupported", should not happen
        }
        
        $thumbnailFilename = static::THUMB_DIRECTORY . $desiredW . "x" . $desiredH . "_" . $filename;
        
        // Get original image size
        $originalW = imagesx($originalImage);
        $originalH = imagesy($originalImage);
    
        $originalRatio = $originalW/$originalH;
        
        // Get sizes of original image but in the desired ratio
        if ($desiredW == 0) { // reduce to height
            $desiredW = $originalRatio * $desiredH;
            $thumbnailFilename = static::THUMB_DIRECTORY . "wx" . $desiredH . "_" . $filename;
        } else if ($desiredH == 0) { // reduce to width
            $desiredH = $desiredW / $originalRatio;
            $thumbnailFilename = static::THUMB_DIRECTORY . $desiredW . "xh_" . $filename;
        }
        
        $desiredRatio = $desiredW/$desiredH;
        
        $widthInRatio = $originalW;
        $heightInRatio = $originalH;
        $initialX = 0;
        $initialY = 0;
        
        if ($desiredRatio > $originalRatio) { // desired image is "more landscape", match to width
            // $widthInRatio = $originalW;
            $heightInRatio = $widthInRatio / $desiredRatio;
            // $initialX = 0;
            $initialY = ($originalH - $heightInRatio) / 2;
        } else if ($desiredRatio < $originalRatio) { // desired image is "more portrait", match to height
            // $heightInRatio = $originalH;
            $widthInRatio = $desiredRatio * $heightInRatio;
            $initialX = ($originalW - $widthInRatio) / 2;
            // $initialY = 0;
        } /*else { // woohoo! same ratio
            $widthInRatio = $originalW;
            $heightInRatio = $originalH;
            $initialX = 0;
            $initialY = 0;
        }*/
        
        // generate thumbnail
        $thumbnail = @imagecreatetruecolor($desiredW, $desiredH);
        imagecopyresampled($thumbnail, $originalImage, 0, 0, $initialX, $initialY, $desiredW, $desiredH, $widthInRatio, $heightInRatio);
        
        // save thumbnail
        imagejpeg($thumbnail, $thumbnailFilename);
        
    }
    
    protected static function echoPhotoData($filename) {
        
        header('Content-Type: image/jpg');
        header('Content-Length: ' . filesize($filename));
        readfile($filename);
    }
}

?>