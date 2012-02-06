<?php
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
    return $s;
}

$method = strip_tags($_SERVER['REQUEST_METHOD']);
if ($method == 'GET' || $method == 'HEAD')
{
    $ct = strip_tags($_REQUEST['CT']);
    if (isset($ct) && $ct == 'text/plain')
    {
        header("Content-Type: text/plain");
        echo dumpInterestingServerVars(': ', "\n");
    }
    else
    {
?><html><head>
<title>Test Script for LightHttpClient</title>
</head>
<body>
<img src="B.png" width="16" height="16" alt="B symbol"/>
<pre>
<?php echo dumpInterestingServerVars(': ', "\n") ?>
<?php print_r($_SERVER); ?>
</pre>
<ul>
<li><a href="?CT=text/plain">text/plain</a></li>
</ul>
</body>
</html>
<?php
    }
}
else
{
    $qs = '?';
    foreach ($_REQUEST as $key=>$val)
    {
        $qs .= urlencode($key) . '=' . urlencode($val) . '&';
    }
    if (isset($_SERVER['HTTP_HOST']))
    {
        $host = $_SERVER['HTTP_HOST'];
    }
    else
    {
        $host = $_SERVER['SERVER_ADDR'];
    }
    header('Location: http://' . $host . '/' . $_SERVER['PHP_SELF'] . $qs);
}
?>