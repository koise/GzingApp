-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Sep 12, 2025 at 12:05 AM
-- Server version: 10.11.10-MariaDB-log
-- PHP Version: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `u126959096_gzing_admin`
--

-- --------------------------------------------------------

--
-- Table structure for table `email_verifications`
--

CREATE TABLE `email_verifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `verification_token` varchar(255) NOT NULL,
  `verification_code` varchar(10) NOT NULL,
  `type` enum('email_verification','password_reset') NOT NULL DEFAULT 'email_verification',
  `is_verified` tinyint(1) NOT NULL DEFAULT 0,
  `expires_at` datetime NOT NULL,
  `verified_at` datetime DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `navigation_activity_logs`
--

CREATE TABLE `navigation_activity_logs` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `activity_type` enum('navigation_start','navigation_stop','navigation_pause','navigation_resume','route_change','destination_reached') NOT NULL,
  `start_latitude` decimal(10,8) DEFAULT NULL,
  `start_longitude` decimal(11,8) DEFAULT NULL,
  `end_latitude` decimal(10,8) DEFAULT NULL,
  `end_longitude` decimal(11,8) DEFAULT NULL,
  `destination_name` varchar(500) DEFAULT NULL,
  `destination_address` text DEFAULT NULL,
  `route_distance` decimal(10,2) DEFAULT NULL,
  `estimated_duration` int(11) DEFAULT NULL,
  `transport_mode` enum('driving','walking','cycling','transit') DEFAULT NULL,
  `navigation_duration` int(11) DEFAULT NULL,
  `route_instructions` text DEFAULT NULL,
  `waypoints` text DEFAULT NULL,
  `destination_reached` tinyint(1) DEFAULT 0,
  `device_info` text DEFAULT NULL,
  `app_version` varchar(50) DEFAULT NULL,
  `os_version` varchar(50) DEFAULT NULL,
  `additional_data` text DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `navigation_activity_logs_archive`
--

CREATE TABLE `navigation_activity_logs_archive` (
  `id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `activity_type` enum('navigation_start','navigation_stop','navigation_pause','navigation_resume','route_change','destination_reached') DEFAULT NULL,
  `start_latitude` decimal(10,8) DEFAULT NULL,
  `start_longitude` decimal(11,8) DEFAULT NULL,
  `end_latitude` decimal(10,8) DEFAULT NULL,
  `end_longitude` decimal(11,8) DEFAULT NULL,
  `destination_name` varchar(500) DEFAULT NULL,
  `destination_address` text DEFAULT NULL,
  `route_distance` decimal(10,2) DEFAULT NULL,
  `estimated_duration` int(11) DEFAULT NULL,
  `transport_mode` enum('driving','walking','cycling','transit') DEFAULT NULL,
  `navigation_duration` int(11) DEFAULT NULL,
  `route_instructions` text DEFAULT NULL,
  `waypoints` text DEFAULT NULL,
  `destination_reached` tinyint(1) DEFAULT NULL,
  `device_info` text DEFAULT NULL,
  `app_version` varchar(50) DEFAULT NULL,
  `os_version` varchar(50) DEFAULT NULL,
  `additional_data` text DEFAULT NULL,
  `error_message` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `archived_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `routes`
--

CREATE TABLE `routes` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL COMMENT 'Route name (e.g., "Antipolo to Marikina")',
  `description` text DEFAULT NULL COMMENT 'Detailed description of the route',
  `pincount` int(11) NOT NULL DEFAULT 0 COMMENT 'Number of pins/stops on the route',
  `kilometer` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Total distance in kilometers',
  `estimated_total_fare` decimal(10,2) NOT NULL DEFAULT 0.00,
  `map_details` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Map configuration and pin details in JSON format' CHECK (json_valid(`map_details`)),
  `status` enum('active','inactive','maintenance') NOT NULL DEFAULT 'active' COMMENT 'Route status',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'When the route was created',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'When the route was last updated'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Routes management system for transportation routes with map integration';

--
-- Dumping data for table `routes`
--

INSERT INTO `routes` (`id`, `name`, `description`, `pincount`, `kilometer`, `estimated_total_fare`, `map_details`, `status`, `created_at`, `updated_at`) VALUES
(4, 'Antipolo - Padilla', 'Via Olalia Road', 11, 11.30, 71.50, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"Padilla\",\"coordinates\":[121.18845014620507,14.621872837488695],\"lng\":121.18845014620507,\"lat\":14.621872837488695,\"addedAt\":\"2025-09-04T03:26:18.535Z\",\"placeName\":\"Marcos Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-4\",\"number\":2,\"name\":\"Seminaryo\",\"coordinates\":[121.1793206213581,14.622700402746077],\"lng\":121.1793206213581,\"lat\":14.622700402746077,\"addedAt\":\"2025-09-04T03:26:52.807Z\",\"placeName\":\"Marcos Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-5\",\"number\":3,\"name\":\"Langhaya\",\"coordinates\":[121.17618991981271,14.622314192761323],\"lng\":121.17618991981271,\"lat\":14.622314192761323,\"addedAt\":\"2025-09-04T03:26:59.279Z\",\"placeName\":\"Marcos Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-6\",\"number\":4,\"name\":\"Cogeo\",\"coordinates\":[121.16952500369052,14.620856977008245],\"lng\":121.16952500369052,\"lat\":14.620856977008245,\"addedAt\":\"2025-09-04T03:27:07.382Z\",\"placeName\":\"Gsis Avenue Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-7\",\"number\":5,\"name\":\"Gate 1\",\"coordinates\":[121.16459861822904,14.62308987422469],\"lng\":121.16459861822904,\"lat\":14.62308987422469,\"addedAt\":\"2025-09-04T04:09:13.439Z\",\"placeName\":\"Marcos Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-8\",\"number\":6,\"name\":\"Blue Mountain\",\"coordinates\":[121.15263874910715,14.623690288344775],\"lng\":121.15263874910715,\"lat\":14.623690288344775,\"addedAt\":\"2025-09-04T04:32:39.034Z\",\"placeName\":\"Ambrosio F. Neri Street Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-9\",\"number\":7,\"name\":\"SM Cherry\",\"coordinates\":[121.13397527299395,14.624319295070634],\"lng\":121.13397527299395,\"lat\":14.624319295070634,\"addedAt\":\"2025-09-04T04:32:48.145Z\",\"placeName\":\"Marcos Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-10\",\"number\":8,\"name\":\"Masinag\",\"coordinates\":[121.12462225136284,14.625433666061014],\"lng\":121.12462225136284,\"lat\":14.625433666061014,\"addedAt\":\"2025-09-04T04:32:54.689Z\",\"placeName\":\"Marcos Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-11\",\"number\":9,\"name\":\"Tropical\",\"coordinates\":[121.10225596681892,14.621103042709422],\"lng\":121.10225596681892,\"lat\":14.621103042709422,\"addedAt\":\"2025-09-04T04:33:18.505Z\",\"placeName\":\"1 Gunting Street Dela Paz, Marikina, Philippines\",\"address\":\"Marikina\"},{\"id\":\"pin-12\",\"number\":10,\"name\":\"Bacolod Street\",\"coordinates\":[121.10120428776014,14.628221356737754],\"lng\":121.10120428776014,\"lat\":14.628221356737754,\"addedAt\":\"2025-09-04T04:33:31.218Z\",\"placeName\":\"Bacolod Street San Roque, Marikina, Philippines\",\"address\":\"Marikina\"},{\"id\":\"pin-13\",\"number\":11,\"name\":\"Marikina Bayan Terminal\",\"coordinates\":[121.09910164476406,14.632582090185238],\"lng\":121.09910164476406,\"lat\":14.632582090185238,\"addedAt\":\"2025-09-04T04:33:38.105Z\",\"placeName\":\"4 Mayor Juan Chanyungco Street Santa Elena, Marikina, Philippines\",\"address\":\"Marikina\"}],\"center\":{\"lng\":121.15829923752636,\"lat\":14.62424523916664},\"zoom\":13.151412188068157,\"fifo_order\":[\"pin-1\",\"pin-4\",\"pin-5\",\"pin-6\",\"pin-7\",\"pin-8\",\"pin-9\",\"pin-10\",\"pin-11\",\"pin-12\",\"pin-13\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.187873,14.62177],[121.187767,14.622535],[121.179279,14.622718],[121.17618,14.622291],[121.173809,14.622668],[121.169526,14.620862],[121.16496,14.623281],[121.163297,14.624245],[121.158216,14.622864],[121.152664,14.623933],[121.152668,14.623691],[121.15267,14.623591],[121.152813,14.62392],[121.149195,14.623945],[121.144672,14.625843],[121.133973,14.624365],[121.124631,14.625481],[121.102305,14.620896],[121.102044,14.620839],[121.102012,14.628327],[121.101201,14.628267],[121.100789,14.628238],[121.100259,14.632657],[121.099111,14.632512]],\"type\":\"LineString\"}}}}', 'active', '2025-09-04 03:27:10', '2025-09-08 03:17:36'),
(5, 'Antipolo - Marikina Bayan', 'Via Sumulong Highway', 18, 13.20, 81.00, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"Bayan Terminal\",\"coordinates\":[121.17673021745048,14.589428522079501],\"lng\":121.17673021745048,\"lat\":14.589428522079501,\"addedAt\":\"2025-09-04T04:20:00.773Z\",\"placeName\":\"C. Lawis Street San Isidro, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-2\",\"number\":2,\"name\":\"Robinson\",\"coordinates\":[121.17197055497729,14.595197440215557],\"lng\":121.17197055497729,\"lat\":14.595197440215557,\"addedAt\":\"2025-09-04T04:20:11.389Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-3\",\"number\":3,\"name\":\"Dela Paz Elementary School\",\"coordinates\":[121.17243851777386,14.597638243921423],\"lng\":121.17243851777386,\"lat\":14.597638243921423,\"addedAt\":\"2025-09-04T04:20:32.172Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-4\",\"number\":4,\"name\":\"Depot\",\"coordinates\":[121.17617933049138,14.601723313266987],\"lng\":121.17617933049138,\"lat\":14.601723313266987,\"addedAt\":\"2025-09-04T04:20:48.035Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-5\",\"number\":5,\"name\":\"Assumption\",\"coordinates\":[121.17405467206419,14.605407785783186],\"lng\":121.17405467206419,\"lat\":14.605407785783186,\"addedAt\":\"2025-09-04T04:20:57.453Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-6\",\"number\":6,\"name\":\"Check point\",\"coordinates\":[121.17329077464257,14.606560627437176],\"lng\":121.17329077464257,\"lat\":14.606560627437176,\"addedAt\":\"2025-09-04T04:21:05.125Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-7\",\"number\":7,\"name\":\"Starbucks Sumulong Highway\",\"coordinates\":[121.15756330494713,14.611858100447236],\"lng\":121.15756330494713,\"lat\":14.611858100447236,\"addedAt\":\"2025-09-04T04:21:29.219Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-8\",\"number\":8,\"name\":\"Fatima\",\"coordinates\":[121.15225224624368,14.618621714923748],\"lng\":121.15225224624368,\"lat\":14.618621714923748,\"addedAt\":\"2025-09-04T04:21:44.955Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-9\",\"number\":9,\"name\":\"San Benildo\",\"coordinates\":[121.14724631924474,14.620051250795981],\"lng\":121.14724631924474,\"lat\":14.620051250795981,\"addedAt\":\"2025-09-04T04:22:43.844Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-10\",\"number\":10,\"name\":\"Mambugan\",\"coordinates\":[121.14045591951748,14.61878589272564],\"lng\":121.14045591951748,\"lat\":14.61878589272564,\"addedAt\":\"2025-09-04T04:24:36.236Z\",\"placeName\":\"Siruna Road Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-11\",\"number\":11,\"name\":\"Xentro Mall Antipolo\",\"coordinates\":[121.13557119917391,14.617088070448034],\"lng\":121.13557119917391,\"lat\":14.617088070448034,\"addedAt\":\"2025-09-04T04:24:50.348Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-12\",\"number\":12,\"name\":\"Antipolo Hospital System\",\"coordinates\":[121.12784385783362,14.620357622751499],\"lng\":121.12784385783362,\"lat\":14.620357622751499,\"addedAt\":\"2025-09-04T04:25:11.987Z\",\"placeName\":\"226 Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-13\",\"number\":13,\"name\":\"Masinag\",\"coordinates\":[121.12458386668527,14.624917948639975],\"lng\":121.12458386668527,\"lat\":14.624917948639975,\"addedAt\":\"2025-09-04T04:25:21.091Z\",\"placeName\":\"Marcos Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-15\",\"number\":15,\"name\":\"Cupang\",\"coordinates\":[121.12373260128282,14.62879195755697],\"lng\":121.12373260128282,\"lat\":14.62879195755697,\"addedAt\":\"2025-09-04T04:25:44.675Z\",\"placeName\":\"P. Oliveros Street Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-16\",\"number\":16,\"name\":\"Aramis\",\"coordinates\":[121.1115787804743,14.633040753981476],\"lng\":121.1115787804743,\"lat\":14.633040753981476,\"addedAt\":\"2025-09-04T04:26:01.803Z\",\"placeName\":\"492 Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-17\",\"number\":17,\"name\":\"Marikina Valley Hospital\",\"coordinates\":[121.10392867080571,14.634731612385124],\"lng\":121.10392867080571,\"lat\":14.634731612385124,\"addedAt\":\"2025-09-04T04:26:30.803Z\",\"placeName\":\"Sumulong Highway Santo Ni\\u00f1o, Marikina, Philippines\",\"address\":\"Marikina\"},{\"id\":\"pin-18\",\"number\":18,\"name\":\"Bluewave\",\"coordinates\":[121.102335271723,14.634902475640004],\"lng\":121.102335271723,\"lat\":14.634902475640004,\"addedAt\":\"2025-09-04T04:26:47.458Z\",\"placeName\":\"Sumulong Highway Santo Ni\\u00f1o, Marikina, Philippines\",\"address\":\"Marikina\"},{\"id\":\"pin-19\",\"number\":19,\"name\":\"Marikina Bayan Terminal\",\"coordinates\":[121.10021758171348,14.633738438020657],\"lng\":121.10021758171348,\"lat\":14.633738438020657,\"addedAt\":\"2025-09-04T04:27:04.987Z\",\"placeName\":\"Jacamar Street Santa Elena, Marikina, Philippines\",\"address\":\"Marikina\"}],\"center\":{\"lng\":121.11243467193066,\"lat\":14.629946321689687},\"zoom\":13.58560331633014,\"fifo_order\":[\"pin-1\",\"pin-2\",\"pin-3\",\"pin-4\",\"pin-5\",\"pin-6\",\"pin-7\",\"pin-8\",\"pin-9\",\"pin-10\",\"pin-11\",\"pin-12\",\"pin-13\",\"pin-15\",\"pin-16\",\"pin-17\",\"pin-18\",\"pin-19\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.176714,14.589456],[121.175718,14.589504],[121.175562,14.59373],[121.173767,14.59421],[121.172318,14.593279],[121.171868,14.593242],[121.17186,14.59519],[121.171843,14.596459],[121.172416,14.59765],[121.173561,14.599779],[121.175315,14.600702],[121.176095,14.601707],[121.176178,14.601702],[121.176375,14.60169],[121.176095,14.601707],[121.174045,14.605402],[121.173487,14.606338],[121.173263,14.60653],[121.167625,14.607527],[121.165365,14.607056],[121.163345,14.609089],[121.160545,14.609207],[121.159226,14.610732],[121.157539,14.611884],[121.156407,14.613055],[121.154166,14.612803],[121.152481,14.613414],[121.151547,14.614248],[121.151217,14.615415],[121.15239,14.616299],[121.152704,14.61701],[121.152267,14.618589],[121.151669,14.619071],[121.148743,14.618898],[121.147251,14.620057],[121.146671,14.620328],[121.145737,14.620076],[121.144945,14.618715],[121.14409,14.618225],[121.143629,14.617256],[121.142454,14.616296],[121.14131,14.616735],[121.140451,14.618747],[121.139552,14.618831],[121.137239,14.617272],[121.135569,14.617146],[121.130546,14.618615],[121.128705,14.619513],[121.127846,14.62036],[121.125007,14.623325],[121.124562,14.624916],[121.12373,14.628778],[121.12373,14.628778],[121.120233,14.629571],[121.116054,14.632119],[121.111551,14.633022],[121.103889,14.634563],[121.102335,14.634903],[121.102,14.634976],[121.101763,14.633801],[121.101028,14.63395],[121.100888,14.633324],[121.100156,14.633563]],\"type\":\"LineString\"}}}}', 'active', '2025-09-04 04:21:47', '2025-09-04 04:27:11'),
(6, 'CMA - Antipolo Bayan', 'Via Olalia Road', 8, 4.60, 38.00, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"CMA Terminal\",\"coordinates\":[121.16979881670517,14.618240457616665],\"lng\":121.16979881670517,\"lat\":14.618240457616665,\"addedAt\":\"2025-09-04T04:36:59.888Z\",\"placeName\":\"Olalia Road Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-2\",\"number\":2,\"name\":\"Antipolo National Highschool\",\"coordinates\":[121.17049909172283,14.616056037341835],\"lng\":121.17049909172283,\"lat\":14.616056037341835,\"addedAt\":\"2025-09-04T04:37:35.857Z\",\"placeName\":\"Olalia Road Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-3\",\"number\":3,\"name\":\"Check Point\",\"coordinates\":[121.17343807422566,14.606494311935933],\"lng\":121.17343807422566,\"lat\":14.606494311935933,\"addedAt\":\"2025-09-04T04:37:50.880Z\",\"placeName\":\"Olalia Road Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-7\",\"number\":7,\"name\":\"Assumption Hospital\",\"coordinates\":[121.17412678538136,14.60535530661187],\"lng\":121.17412678538136,\"lat\":14.60535530661187,\"addedAt\":\"2025-09-04T04:38:44.544Z\",\"placeName\":\"Sumulong Highway Cupang, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-8\",\"number\":8,\"name\":\"Dela Paz Elementary School\",\"coordinates\":[121.17244052621231,14.597580032660517],\"lng\":121.17244052621231,\"lat\":14.597580032660517,\"addedAt\":\"2025-09-04T04:38:54.712Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-9\",\"number\":9,\"name\":\"Robinson Mall\",\"coordinates\":[121.17169583952494,14.59527389187322],\"lng\":121.17169583952494,\"lat\":14.59527389187322,\"addedAt\":\"2025-09-04T04:39:04.609Z\",\"placeName\":\"Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-10\",\"number\":10,\"name\":\"Uni Oil\",\"coordinates\":[121.17557268349168,14.59352826091137],\"lng\":121.17557268349168,\"lat\":14.59352826091137,\"addedAt\":\"2025-09-04T04:39:33.225Z\",\"placeName\":\"M. L. Quezon Street Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-11\",\"number\":11,\"name\":\"Antipolo Bayan Terminal\",\"coordinates\":[121.17520567827171,14.588759691304702],\"lng\":121.17520567827171,\"lat\":14.588759691304702,\"addedAt\":\"2025-09-04T04:39:58.424Z\",\"placeName\":\"A. Masangkay Street Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"}],\"center\":{\"lng\":121.1756,\"lat\":14.6255},\"zoom\":13,\"fifo_order\":[\"pin-1\",\"pin-2\",\"pin-3\",\"pin-7\",\"pin-8\",\"pin-9\",\"pin-10\",\"pin-11\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.169622,14.618237],[121.169609,14.618935],[121.169001,14.618628],[121.169012,14.617035],[121.16932,14.617122],[121.16936,14.617033],[121.169709,14.617166],[121.170499,14.616055],[121.172252,14.61434],[121.171704,14.612932],[121.172238,14.611348],[121.173429,14.61004],[121.17342,14.606487],[121.174085,14.605333],[121.17608,14.601616],[121.175315,14.600702],[121.173561,14.599779],[121.172391,14.597607],[121.171764,14.59642],[121.171603,14.596351],[121.171656,14.595269],[121.171809,14.592938],[121.173776,14.59407],[121.175564,14.593606],[121.175565,14.593528],[121.175718,14.589504],[121.175491,14.589511],[121.175388,14.588744]],\"type\":\"LineString\"}}}}', 'active', '2025-09-04 04:40:01', '2025-09-04 04:40:21'),
(12, 'Antipolo - Tikling', 'Via - Tikling', 6, 3.90, 34.50, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"Terminal\",\"coordinates\":[121.17034144398866,14.58816887902637],\"lng\":121.17034144398866,\"lat\":14.58816887902637,\"addedAt\":\"2025-09-07T22:44:23.442Z\",\"placeName\":\"Pascual Oliveros Street Mayamot, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-2\",\"number\":2,\"name\":\"Ashford\",\"coordinates\":[121.16565271248817,14.587639893498277],\"lng\":121.16565271248817,\"lat\":14.587639893498277,\"addedAt\":\"2025-09-07T22:44:32.202Z\",\"placeName\":\"Antipolo Diversion Road 2 Mambugan, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-3\",\"number\":3,\"name\":\"Eastern Hotel\",\"coordinates\":[121.15840203591955,14.58745537428625],\"lng\":121.15840203591955,\"lat\":14.58745537428625,\"addedAt\":\"2025-09-07T22:44:45.347Z\",\"placeName\":\"Antipolo Diversion Road 1 Mambugan, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-4\",\"number\":4,\"name\":\"Shell\",\"coordinates\":[121.15305078432334,14.586089968101419],\"lng\":121.15305078432334,\"lat\":14.586089968101419,\"addedAt\":\"2025-09-07T22:44:53.890Z\",\"placeName\":\"1870 Corazon C. Aquino Avenue Mambugan, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-5\",\"number\":5,\"name\":\"Harris\",\"coordinates\":[121.15189787661308,14.583874858855893],\"lng\":121.15189787661308,\"lat\":14.583874858855893,\"addedAt\":\"2025-09-07T22:45:00.762Z\",\"placeName\":\"Corazon C. Aquino Avenue Dolores, Taytay, Rizal, Philippines\",\"address\":\"Taytay\"},{\"id\":\"pin-6\",\"number\":6,\"name\":\"Tikling\",\"coordinates\":[121.14309048307581,14.577143483842278],\"lng\":121.14309048307581,\"lat\":14.577143483842278,\"addedAt\":\"2025-09-07T22:45:12.810Z\",\"placeName\":\"Drive-Through Dolores, Taytay, Rizal, Philippines\",\"address\":\"Taytay\"}],\"center\":{\"lng\":121.13400908995942,\"lat\":14.589188278882105},\"zoom\":12.849216165875657,\"fifo_order\":[\"pin-1\",\"pin-2\",\"pin-3\",\"pin-4\",\"pin-5\",\"pin-6\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.170343,14.588169],[121.170394,14.587753],[121.170195,14.587353],[121.169059,14.586863],[121.16829,14.586922],[121.167157,14.587754],[121.166115,14.587922],[121.165678,14.587626],[121.165482,14.587354],[121.164973,14.587102],[121.163298,14.586613],[121.162407,14.586518],[121.159833,14.58681],[121.158864,14.587666],[121.158425,14.58741],[121.157584,14.587578],[121.157125,14.586909],[121.156119,14.586638],[121.154156,14.586804],[121.153394,14.586456],[121.153047,14.586092],[121.152861,14.585576],[121.152962,14.584672],[121.152445,14.584418],[121.151897,14.583876],[121.149665,14.581723],[121.147928,14.581347],[121.147423,14.580853],[121.146846,14.579742],[121.145599,14.579425],[121.144331,14.577973],[121.143239,14.577714],[121.143052,14.577625],[121.143136,14.577222],[121.142885,14.577175],[121.142723,14.57702],[121.142898,14.57684],[121.143096,14.577122]],\"type\":\"LineString\"}}}}', 'inactive', '2025-09-07 22:41:53', '2025-09-07 22:45:24'),
(16, 'Evson Baccay', 'Tikling to Beverly', 2, 2.20, 26.00, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"Tikling\",\"coordinates\":[121.14335747970892,14.577662069850078],\"lng\":121.14335747970892,\"lat\":14.577662069850078,\"addedAt\":\"2025-09-08T03:19:29.087Z\",\"placeName\":\"Corazon C. Aquino Avenue Dolores, Taytay, Rizal, Philippines\",\"address\":\"Taytay\"},{\"id\":\"pin-2\",\"number\":2,\"name\":\"Beverly\",\"coordinates\":[121.15926835923347,14.586810172317385],\"lng\":121.15926835923347,\"lat\":14.586810172317385,\"addedAt\":\"2025-09-08T03:19:47.269Z\",\"placeName\":\"Corazon C. Aquino Avenue Mambugan, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"}],\"center\":{\"lng\":121.14725308688719,\"lat\":14.58287707827148},\"zoom\":14.126001227944633,\"fifo_order\":[\"pin-1\",\"pin-2\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.143368,14.577649],[121.144331,14.577973],[121.145599,14.579425],[121.146016,14.579581],[121.146652,14.579653],[121.146912,14.579794],[121.147423,14.580853],[121.147928,14.581347],[121.148236,14.581457],[121.149463,14.581601],[121.150018,14.582019],[121.151305,14.583188],[121.152287,14.584289],[121.152962,14.584672],[121.153007,14.584963],[121.152881,14.585352],[121.152873,14.585686],[121.153097,14.586172],[121.153394,14.586456],[121.154156,14.586804],[121.155965,14.586631],[121.156824,14.586795],[121.159252,14.586823]],\"type\":\"LineString\"}}}}', 'active', '2025-09-08 03:19:58', '2025-09-08 03:21:03'),
(17, 'evson - antipolo', 'asdsa', 2, 4.60, 38.00, '{\"pins\":[{\"id\":\"pin-1\",\"number\":1,\"name\":\"asda\",\"coordinates\":[121.1705867877626,14.584511040230922],\"lng\":121.1705867877626,\"lat\":14.584511040230922,\"addedAt\":\"2025-09-08T03:20:12.718Z\",\"placeName\":\"Adarna Street Mambugan, Antipolo City, Rizal, Philippines\",\"address\":\"Antipolo City\"},{\"id\":\"pin-2\",\"number\":2,\"name\":\"asdas\",\"coordinates\":[121.14043866463277,14.577771537622809],\"lng\":121.14043866463277,\"lat\":14.577771537622809,\"addedAt\":\"2025-09-08T03:20:14.437Z\",\"placeName\":\"Pearl Avenue Dolores, Taytay, Rizal, Philippines\",\"address\":\"Taytay\"}],\"center\":{\"lng\":121.1756,\"lat\":14.6255},\"zoom\":13,\"fifo_order\":[\"pin-1\",\"pin-2\"],\"route_line\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"type\":\"Feature\",\"properties\":[],\"geometry\":{\"coordinates\":[[121.170592,14.584397],[121.170368,14.584336],[121.170524,14.583476],[121.168811,14.583129],[121.168679,14.586392],[121.16829,14.586922],[121.16731,14.5877],[121.166115,14.587922],[121.164973,14.587102],[121.163298,14.586613],[121.162407,14.586518],[121.159833,14.58681],[121.158864,14.587666],[121.158357,14.587387],[121.157584,14.587578],[121.157125,14.586909],[121.15634,14.586678],[121.154156,14.586804],[121.153394,14.586456],[121.152873,14.585686],[121.152962,14.584672],[121.152287,14.584289],[121.149773,14.581804],[121.149463,14.581601],[121.147928,14.581347],[121.147423,14.580853],[121.146846,14.579742],[121.145599,14.579425],[121.144331,14.577973],[121.143239,14.577714],[121.140444,14.578591],[121.140413,14.578495],[121.140913,14.578339],[121.140502,14.577697]],\"type\":\"LineString\"}}}}', 'inactive', '2025-09-08 03:20:21', '2025-09-08 03:21:20');

-- --------------------------------------------------------

--
-- Table structure for table `sos_contacts`
--

CREATE TABLE `sos_contacts` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT 'Foreign key to users table',
  `name` varchar(255) NOT NULL COMMENT 'Contact name',
  `phone_number` varchar(20) NOT NULL COMMENT 'Contact phone number',
  `relationship` varchar(100) NOT NULL COMMENT 'Relationship to user (e.g., Family, Friend, Emergency)',
  `is_primary` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Whether this is the primary emergency contact',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'When the contact was created',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'When the contact was last updated',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT 'Soft delete timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Emergency SOS contacts for users';

--
-- Dumping data for table `sos_contacts`
--

INSERT INTO `sos_contacts` (`id`, `user_id`, `name`, `phone_number`, `relationship`, `is_primary`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 10, 'Evson', '09504893347', 'Mother', 0, '2025-09-08 02:44:04', '2025-09-08 18:27:33', '2025-09-08 18:26:56'),
(5, 10, 'Evson', '+639504893347', 'Friend', 0, '2025-09-08 21:34:31', '2025-09-08 21:42:06', NULL),
(6, 10, 'Mama', '+639934469840', 'Family', 0, '2025-09-08 21:42:06', '2025-09-08 22:37:40', NULL),
(7, 10, 'Evson', '+639504893347', 'Friend', 0, '2025-09-08 22:37:40', '2025-09-11 23:20:02', NULL),
(8, 10, 'Emergency Contact', '+1234567890', 'Family', 0, '2025-09-11 23:18:56', '2025-09-11 23:18:56', NULL),
(9, 10, 'Emergency Contact', '+1234567890', 'Family', 1, '2025-09-11 23:20:02', '2025-09-11 23:20:02', NULL),
(10, 31, 'Emergency Contact 1', '+1234567890', 'Family', 1, '2025-09-11 23:23:00', '2025-09-11 23:23:00', NULL),
(11, 31, 'Emergency Contact 2', '+1234567891', 'Friend', 0, '2025-09-11 23:23:00', '2025-09-11 23:23:00', NULL),
(12, 31, 'Emergency Contact 3', '+1234567892', 'Doctor', 0, '2025-09-11 23:23:00', '2025-09-11 23:23:00', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL COMMENT 'User first name',
  `last_name` varchar(100) NOT NULL COMMENT 'User last name',
  `email` varchar(255) NOT NULL COMMENT 'User email address (unique)',
  `phone_number` varchar(20) DEFAULT NULL COMMENT 'User phone number in Philippine format (+639XXXXXXXXX)',
  `username` varchar(50) NOT NULL COMMENT 'Username for login (unique)',
  `password_hash` varchar(255) NOT NULL COMMENT 'Hashed password',
  `role` enum('admin','moderator','user') NOT NULL DEFAULT 'user' COMMENT 'User role/permission level',
  `status` enum('active','inactive','suspended') NOT NULL DEFAULT 'active' COMMENT 'User account status',
  `notes` text DEFAULT NULL COMMENT 'Additional notes about the user',
  `last_login` timestamp NULL DEFAULT NULL COMMENT 'Last login timestamp',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Account creation timestamp',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Last update timestamp',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT 'Soft delete timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts, authentication, and profile information for GzingAdmin system';

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `phone_number`, `username`, `password_hash`, `role`, `status`, `notes`, `last_login`, `created_at`, `updated_at`, `deleted_at`) VALUES
(5, 'Admin', 'User', 'admin@example.com', '+639555555555', 'admin', '.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'moderator', 'active', NULL, NULL, '2025-09-04 02:31:24', '2025-09-04 03:54:11', NULL),
(6, 'Moderator', 'User', 'moderator@example.com', NULL, 'moderator', '.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'moderator', 'suspended', NULL, NULL, '2025-09-04 02:31:24', '2025-09-04 03:52:38', '2025-09-04 03:52:38'),
(7, 'Dev', 'Moderator', 'dev@example.com', '+639987654321', 'dev', '$2y$10$DtP2RUwGVmBBcvrTKycl3usCni9snlLfyh7CJB5a36YfcziRVgg4a', 'moderator', 'active', 'Moderator account', '2025-09-04 02:45:33', '2025-09-04 02:43:25', '2025-09-04 02:45:33', NULL),
(8, 'Koi Updated', 'Developer', 'koi.updated@example.com', '+639123456789', 'koi', '$2y$10$MFwylftF22L0VOhtsAHDuuHtqRsMKZKxmPZecedIwzkLnRLxoOYB.', 'admin', 'active', 'Updated via API test', '2025-09-08 03:15:15', '2025-09-04 02:43:57', '2025-09-08 03:15:15', NULL),
(9, 'EvsonN', 'Bacay', 'Evson@gmail.com', '+639581283912', 'evson', '$2y$10$d0v6zHKsRqRUm9CK5Vd4Fe2XqMfaKxYOZiY8a0ItlyaNL7RDFhKVe', 'admin', 'active', 'Added', '2025-09-08 03:07:38', '2025-09-04 04:18:20', '2025-09-08 03:07:38', NULL),
(32, 'Test', 'User', 'test1757634512@example.com', NULL, 'testuser1757634512', '$2y$10$iAZJYLZjPhMrxqmWRCmIquSAso3/2N036DtM4Z5askAjPVPAY/eoW', 'user', 'active', NULL, '2025-09-11 23:48:36', '2025-09-11 23:48:36', '2025-09-11 23:48:36', NULL),
(33, 'Test', 'User', 'testuser1757634557@example.com', NULL, 'testuser1757634557', '$2y$10$LbX8XkSp4cVwliSgjqILpuTeG149pk4gCygKvop/vzU3QtCD.t6MO', 'user', 'active', NULL, NULL, '2025-09-11 23:49:21', '2025-09-11 23:49:21', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_activity_logs`
--

CREATE TABLE `user_activity_logs` (
  `id` int(11) NOT NULL,
  `log_type` varchar(50) NOT NULL COMMENT 'Type of log (user_create, user_update, user_delete, user_login, user_logout, page_access)',
  `log_level` varchar(20) NOT NULL DEFAULT 'info' COMMENT 'Log level (info, warning, error, debug)',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'Name of the user performing the action',
  `action` varchar(255) NOT NULL COMMENT 'Description of the action performed',
  `message` text NOT NULL COMMENT 'Detailed message about the action',
  `user_agent` text DEFAULT NULL COMMENT 'User agent string',
  `additional_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Additional data in JSON format' CHECK (json_valid(`additional_data`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'When the log was created'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User activity and system logs (IP address removed for privacy)';

--
-- Dumping data for table `user_activity_logs`
--

INSERT INTO `user_activity_logs` (`id`, `log_type`, `log_level`, `user_name`, `action`, `message`, `user_agent`, `additional_data`, `created_at`) VALUES
(1, 'user_logout', 'info', 'Admin User', 'User Logout', 'User Admin User logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":1,\"username\":\"admin\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":2323,\"logout_reason\":\"user_initiated\"}', '2025-09-04 02:40:20'),
(2, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 04:40:24\"}', '2025-09-04 02:40:24'),
(3, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 04:40:27\"}', '2025-09-04 02:40:27'),
(4, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin@example.com', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin@example.com\",\"success\":false,\"timestamp\":\"2025-09-04 04:40:37\"}', '2025-09-04 02:40:37'),
(5, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin@example.com', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin@example.com\",\"success\":false,\"timestamp\":\"2025-09-04 04:40:42\"}', '2025-09-04 02:40:42'),
(6, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-04 04:45:12\"}', '2025-09-04 02:45:12'),
(7, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-04 04:45:17\"}', '2025-09-04 02:45:17'),
(8, 'user_login', 'info', 'Dev Moderator', 'User Login', 'User dev logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":7,\"role\":\"moderator\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"dev\",\"success\":true,\"timestamp\":\"2025-09-04 04:45:33\"}', '2025-09-04 02:45:33'),
(9, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"1\",\"action\":\"route_create\"}', '2025-09-04 03:03:46'),
(10, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"2\",\"action\":\"route_create\"}', '2025-09-04 03:10:44'),
(11, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"3\",\"action\":\"route_create\"}', '2025-09-04 03:21:42'),
(12, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"4\",\"action\":\"route_create\"}', '2025-09-04 03:27:10'),
(13, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"1\",\"action\":\"route_update\"}', '2025-09-04 03:40:40'),
(14, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"1\",\"action\":\"route_update\"}', '2025-09-04 03:40:59'),
(15, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"1\",\"action\":\"route_update\"}', '2025-09-04 03:41:11'),
(16, 'route_management', 'info', 'system', 'route_delete', 'Route \'Test Road-Following Route haha\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"1\",\"action\":\"route_delete\"}', '2025-09-04 03:41:46'),
(17, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"2\",\"action\":\"route_update\"}', '2025-09-04 03:42:00'),
(18, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"4\",\"action\":\"route_update\"}', '2025-09-04 03:42:26'),
(19, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"3\",\"action\":\"route_update\"}', '2025-09-04 03:42:37'),
(20, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"3\",\"action\":\"route_update\"}', '2025-09-04 03:43:04'),
(21, 'user_logout', 'info', 'Dev Moderator', 'User Logout', 'User Dev Moderator logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":7,\"username\":\"dev\",\"role\":\"moderator\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":3480,\"logout_reason\":\"user_initiated\"}', '2025-09-04 03:43:33'),
(22, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 05:43:37\"}', '2025-09-04 03:43:37'),
(23, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":582,\"logout_reason\":\"user_initiated\"}', '2025-09-04 03:53:19'),
(24, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 05:53:30\"}', '2025-09-04 03:53:30'),
(25, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 05:53:33\"}', '2025-09-04 03:53:33'),
(26, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"3\",\"action\":\"route_update\"}', '2025-09-04 03:54:48'),
(27, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"2\",\"action\":\"route_update\"}', '2025-09-04 04:01:29'),
(28, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"2\",\"action\":\"route_update\"}', '2025-09-04 04:07:53'),
(29, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"4\",\"action\":\"route_update\"}', '2025-09-04 04:09:19'),
(30, 'route_management', 'info', 'system', 'route_delete', 'Route \'Busog sa milo\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"2\",\"action\":\"route_delete\"}', '2025-09-04 04:11:50'),
(31, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":1109,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:12:02'),
(32, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:12:34\"}', '2025-09-04 04:12:34'),
(33, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":147,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:15:01'),
(34, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:15:13\"}', '2025-09-04 04:15:13'),
(35, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":122,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:17:15'),
(36, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:17:18\"}', '2025-09-04 04:17:18'),
(37, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":7,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:17:25'),
(38, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:17:38\"}', '2025-09-04 04:17:38'),
(39, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"5\",\"action\":\"route_create\"}', '2025-09-04 04:21:47'),
(40, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"5\",\"action\":\"route_update\"}', '2025-09-04 04:22:12'),
(41, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"5\",\"action\":\"route_update\"}', '2025-09-04 04:22:46'),
(42, 'route_management', 'info', 'system', 'route_delete', 'Route \'Busog sa milo\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"3\",\"action\":\"route_delete\"}', '2025-09-04 04:23:54'),
(43, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"5\",\"action\":\"route_update\"}', '2025-09-04 04:27:11'),
(44, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"4\",\"action\":\"route_update\"}', '2025-09-04 04:33:39'),
(45, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"6\",\"action\":\"route_create\"}', '2025-09-04 04:40:01'),
(46, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"route_id\":\"6\",\"action\":\"route_update\"}', '2025-09-04 04:40:21'),
(47, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":1415,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:41:13'),
(48, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:41:36\"}', '2025-09-04 04:41:36'),
(49, 'user_logout', 'info', 'Evson Bacay', 'User Logout', 'User Evson Bacay logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"username\":\"evson\",\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":136,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:43:52'),
(50, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:44:32\"}', '2025-09-04 04:44:32'),
(51, 'user_logout', 'info', 'Evson Bacay', 'User Logout', 'User Evson Bacay logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"username\":\"evson\",\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":55,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:45:27'),
(52, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:29\"}', '2025-09-04 04:45:29'),
(53, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:33\"}', '2025-09-04 04:45:33'),
(54, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:37\"}', '2025-09-04 04:45:37'),
(55, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:43\"}', '2025-09-04 04:45:43'),
(56, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:48\"}', '2025-09-04 04:45:48'),
(57, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-04 06:45:57\"}', '2025-09-04 04:45:57'),
(58, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:46:04\"}', '2025-09-04 04:46:04'),
(59, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":25,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:46:29'),
(60, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:46:33\"}', '2025-09-04 04:46:33'),
(61, 'user_logout', 'info', 'Evson Bacay', 'User Logout', 'User Evson Bacay logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"username\":\"evson\",\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":9,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:46:42'),
(62, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:48:53\"}', '2025-09-04 04:48:53'),
(63, 'user_logout', 'info', 'Evson Bacay', 'User Logout', 'User Evson Bacay logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"username\":\"evson\",\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":13,\"logout_reason\":\"user_initiated\"}', '2025-09-04 04:49:06'),
(64, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:50:35\"}', '2025-09-04 04:50:35'),
(65, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:54:06\"}', '2025-09-04 04:54:06'),
(66, 'user_login', 'info', 'Evson Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":9,\"role\":\"user\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-04 06:55:55\"}', '2025-09-04 04:55:55'),
(67, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 06:56:01\"}', '2025-09-04 04:56:01'),
(68, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-04 06:56:04\"}', '2025-09-04 04:56:04'),
(69, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-04 06:56:07\"}', '2025-09-04 04:56:07'),
(70, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 06:56:12\"}', '2025-09-04 04:56:12'),
(71, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 17:03:40\"}', '2025-09-04 15:03:40'),
(72, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"session_duration\":28,\"logout_reason\":\"user_initiated\"}', '2025-09-04 15:04:08'),
(73, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 17:30:14\"}', '2025-09-04 15:30:14'),
(74, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-04 17:30:19\"}', '2025-09-04 15:30:19'),
(75, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-04 17:30:25\"}', '2025-09-04 15:30:25'),
(76, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-07 18:41:19\"}', '2025-09-07 16:41:19'),
(77, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-07 18:41:22\"}', '2025-09-07 16:41:22'),
(78, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-07 18:41:25\"}', '2025-09-07 16:41:25'),
(79, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-07 18:41:27\"}', '2025-09-07 16:41:27'),
(80, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"::1\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36 Edg\\/139.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 18:41:38\"}', '2025-09-07 16:41:38'),
(0, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-07 18:08:04\"}', '2025-09-07 18:08:04'),
(0, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 18:08:07\"}', '2025-09-07 18:08:07'),
(0, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"session_duration\":4076,\"logout_reason\":\"user_initiated\"}', '2025-09-07 19:16:03'),
(0, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-07 19:16:17\"}', '2025-09-07 19:16:17'),
(0, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 19:16:21\"}', '2025-09-07 19:16:21'),
(0, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"session_duration\":329,\"logout_reason\":\"user_initiated\"}', '2025-09-07 19:21:50'),
(0, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 19:21:53\"}', '2025-09-07 19:21:53'),
(0, 'user_logout', 'info', 'Koi Developer', 'User Logout', 'User Koi Developer logged out', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"username\":\"koi\",\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"session_duration\":3755,\"logout_reason\":\"user_initiated\"}', '2025-09-07 20:24:28'),
(0, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user admin', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"admin\",\"success\":false,\"timestamp\":\"2025-09-07 20:29:50\"}', '2025-09-07 20:29:50'),
(0, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user test', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"test\",\"success\":false,\"timestamp\":\"2025-09-07 20:29:56\"}', '2025-09-07 20:29:56'),
(0, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 20:29:59\"}', '2025-09-07 20:29:59'),
(0, 'user_login', 'info', 'Koi Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-07 21:01:48\"}', '2025-09-07 21:01:48'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:13:09'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:16:38'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:18:54'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_update\"}', '2025-09-07 22:25:53'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_update\"}', '2025-09-07 22:25:56'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_update\"}', '2025-09-07 22:26:14'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_update\"}', '2025-09-07 22:26:25'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_update\"}', '2025-09-07 22:26:52'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":4,\"action\":\"route_update\"}', '2025-09-07 22:31:07'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":4,\"action\":\"route_update\"}', '2025-09-07 22:31:45'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Antipolo - Baguio\' deleted successfully', 'Unknown', '{\"route_id\":0,\"action\":\"route_delete\"}', '2025-09-07 22:33:50'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:34:17'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Delete Test Route 1757284454\' deleted successfully', 'Unknown', '{\"route_id\":0,\"action\":\"route_delete\"}', '2025-09-07 22:34:17'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:34:58'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Delete Test Route 1757284495\' deleted successfully', 'Unknown', '{\"route_id\":0,\"action\":\"route_delete\"}', '2025-09-07 22:34:58'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:36:25'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Antipolo - Baguio\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":0,\"action\":\"route_delete\"}', '2025-09-07 22:36:37'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:37:18'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"0\",\"action\":\"route_create\"}', '2025-09-07 22:39:16'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'ID Test Route 1757284753\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":8,\"action\":\"route_delete\"}', '2025-09-07 22:41:13'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Antipolo - Baguio\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":7,\"action\":\"route_delete\"}', '2025-09-07 22:41:20'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"12\",\"action\":\"route_create\"}', '2025-09-07 22:41:53'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":12,\"action\":\"route_update\"}', '2025-09-07 22:42:22'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"13\",\"action\":\"route_create\"}', '2025-09-07 22:42:26');
INSERT INTO `user_activity_logs` (`id`, `log_type`, `log_level`, `user_name`, `action`, `message`, `user_agent`, `additional_data`, `created_at`) VALUES
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Workaround Test Route 1757284944\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":13,\"action\":\"route_delete\"}', '2025-09-07 22:42:55'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Unknown', '{\"route_id\":\"14\",\"action\":\"route_create\"}', '2025-09-07 22:43:13'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":12,\"action\":\"route_update\"}', '2025-09-07 22:45:24'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Improved Test Route 1757284991\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":14,\"action\":\"route_delete\"}', '2025-09-07 22:45:52'),
(0, 'user_login', 'info', 'Koi Updated Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-08 01:01:32\"}', '2025-09-08 01:01:32'),
(0, 'user_login', 'info', 'EvsonN Bacay', 'User Login', 'User evson logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"user_id\":9,\"role\":\"admin\",\"ip_address\":\"112.204.125.127\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/139.0.0.0 Safari\\/537.36\",\"email\":\"evson\",\"success\":true,\"timestamp\":\"2025-09-08 03:07:38\"}', '2025-09-08 03:07:38'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"route_id\":6,\"action\":\"route_update\"}', '2025-09-08 03:14:45'),
(0, 'user_login', 'warning', 'Unknown User', 'Failed Login Attempt', 'Failed login attempt for user koi', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"reason\":\"Invalid credentials\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":false,\"timestamp\":\"2025-09-08 03:15:10\"}', '2025-09-08 03:15:10'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"route_id\":4,\"action\":\"route_update\"}', '2025-09-08 03:15:15'),
(0, 'user_login', 'info', 'Koi Updated Developer', 'User Login', 'User koi logged in successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"user_id\":8,\"role\":\"admin\",\"ip_address\":\"112.204.103.23\",\"user_agent\":\"Mozilla\\/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/140.0.0.0 Safari\\/537.36 Edg\\/140.0.0.0\",\"email\":\"koi\",\"success\":true,\"timestamp\":\"2025-09-08 03:15:15\"}', '2025-09-08 03:15:15'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":4,\"action\":\"route_update\"}', '2025-09-08 03:17:16'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":4,\"action\":\"route_update\"}', '2025-09-08 03:17:36'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"15\",\"action\":\"route_create\"}', '2025-09-08 03:18:23'),
(0, 'route_management', 'info', 'system', 'route_delete', 'Route \'Taytay - Evson\' deleted successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":15,\"action\":\"route_delete\"}', '2025-09-08 03:18:54'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"route_id\":\"16\",\"action\":\"route_create\"}', '2025-09-08 03:19:58'),
(0, 'route_management', 'info', 'system', 'route_create', 'Route created successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 Edg/140.0.0.0', '{\"route_id\":\"17\",\"action\":\"route_create\"}', '2025-09-08 03:20:21'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"route_id\":16,\"action\":\"route_update\"}', '2025-09-08 03:21:03'),
(0, 'route_management', 'info', 'system', 'route_update', 'Route updated successfully', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36', '{\"route_id\":17,\"action\":\"route_update\"}', '2025-09-08 03:21:20');

-- --------------------------------------------------------

--
-- Table structure for table `user_activity_logs_archive`
--

CREATE TABLE `user_activity_logs_archive` (
  `id` int(11) NOT NULL,
  `log_type` varchar(50) NOT NULL,
  `log_level` varchar(20) NOT NULL DEFAULT 'info',
  `user_name` varchar(255) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `user_agent` text DEFAULT NULL,
  `additional_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`additional_data`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `archived_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'When the log was archived'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Archived user activity and system logs (IP address removed for privacy)';

-- --------------------------------------------------------

--
-- Table structure for table `user_statistics`
--

CREATE TABLE `user_statistics` (
  `total_users` bigint(21) DEFAULT NULL,
  `active_users` bigint(21) DEFAULT NULL,
  `inactive_users` bigint(21) DEFAULT NULL,
  `suspended_users` bigint(21) DEFAULT NULL,
  `admin_users` bigint(21) DEFAULT NULL,
  `moderator_users` bigint(21) DEFAULT NULL,
  `regular_users` bigint(21) DEFAULT NULL,
  `recent_logins_7d` bigint(21) DEFAULT NULL,
  `recent_logins_30d` bigint(21) DEFAULT NULL,
  `new_users_30d` bigint(21) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `navigation_activity_logs`
--
ALTER TABLE `navigation_activity_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_user_name` (`user_name`),
  ADD KEY `idx_activity_type` (`activity_type`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_user_activity` (`user_id`,`activity_type`),
  ADD KEY `idx_date_range` (`created_at`,`user_id`);

--
-- Indexes for table `navigation_activity_logs_archive`
--
ALTER TABLE `navigation_activity_logs_archive`
  ADD KEY `idx_archived_user_id` (`user_id`),
  ADD KEY `idx_archived_created_at` (`created_at`),
  ADD KEY `idx_archived_activity_type` (`activity_type`);

--
-- Indexes for table `routes`
--
ALTER TABLE `routes`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `sos_contacts`
--
ALTER TABLE `sos_contacts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_is_primary` (`is_primary`),
  ADD KEY `idx_deleted_at` (`deleted_at`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `navigation_activity_logs`
--
ALTER TABLE `navigation_activity_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `routes`
--
ALTER TABLE `routes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `sos_contacts`
--
ALTER TABLE `sos_contacts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `navigation_activity_logs`
--
ALTER TABLE `navigation_activity_logs`
  ADD CONSTRAINT `navigation_activity_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
