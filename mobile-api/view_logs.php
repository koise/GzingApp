<?php
// Simple log viewer for debugging
header('Content-Type: text/plain');

$log_file = __DIR__ . '/logs/navigation_routes_errors.log';

if (file_exists($log_file)) {
    echo "=== NAVIGATION ROUTES API LOGS ===\n";
    echo "File: $log_file\n";
    echo "Last modified: " . date('Y-m-d H:i:s', filemtime($log_file)) . "\n";
    echo "Size: " . filesize($log_file) . " bytes\n";
    echo "=====================================\n\n";
    
    $logs = file_get_contents($log_file);
    echo $logs;
} else {
    echo "Log file not found: $log_file\n";
    echo "This means no API calls have been made yet or logging is not working.\n";
}

echo "\n\n=== RECENT API CALLS ===\n";
echo "Test the API with these URLs:\n";
echo "1. Get routes: https://powderblue-pig-261057.hostingersite.com/mobile-api/endpoints/navigation-routes/get_navigation_routes.php?user_id=34\n";
echo "2. Test database: https://powderblue-pig-261057.hostingersite.com/mobile-api/test_database.php\n";
echo "3. View logs: https://powderblue-pig-261057.hostingersite.com/mobile-api/view_logs.php\n";
?>
