<?php
//-----------------------------------------------------------------------------
// The MIT License
//
// Copyright (c) 2012 Rick Beton <rick@bigbeeconsultants.co.uk>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//-----------------------------------------------------------------------------
//print_r($_REQUEST);
//print_r($_COOKIE);

function getInterestingServerVars() {
    $svrKeys = array();
    foreach ($_SERVER as $key=>$val) {
        if (strpos($key, "REQUEST_") === 0 ||
            strpos($key, "CONTENT_") === 0 ||
            strpos($key, "QUERY_") === 0 ||
            strpos($key, "SERVER_") === 0 ||
            strpos($key, "HTTP_") === 0) {
            array_push($svrKeys, $key);
        }
    }
    sort($svrKeys);
    return $svrKeys;
}

function dumpInterestingServerVars($assign, $separator) {
    $s = '';
    foreach (getInterestingServerVars() as $key) {
        $val = $_SERVER[$key];
        $s .= "$key$assign$val$separator";
    }
    foreach ($_COOKIE as $key => $val) {
        $s .= "COOKIE: $key$assign$val$separator";
    }
    foreach ($_GET as $key => $val) {
        $s .= "GET: $key$assign$val$separator";
    }
    foreach ($_POST as $key => $val) {
        $s .= "POST: $key$assign$val$separator";
    }
    return $s;
}

$method = strip_tags($_SERVER['REQUEST_METHOD']);
header("Content-Type: text/plain");
echo dumpInterestingServerVars(': ', "\n");

/* PUT data comes in on the stdin stream */
$putdata = fopen("php://input", "r");
if ($putdata !== FALSE) {
    while ($data = fread($putdata, 1024)) {
        echo "PUT: " . $data;
    }
    fclose($putdata);
}
?>