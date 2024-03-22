-- MySQL dump 10.13  Distrib 8.2.0, for Linux (aarch64)
--
-- Host: localhost    Database: example_db
-- ------------------------------------------------------
-- Server version	8.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP DATABASE IF EXISTS `example_db`;
CREATE DATABASE IF NOT EXISTS `example_db`;

USE `example_db`;

--
-- Table structure for table `categories`
--
DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Electronics','2019-08-15 09:23:45','2023-08-15 09:23:45'),(2,'Clothing','2028-06-10 10:30:12','2023-08-15 10:30:12'),(3,'Home & Garden','2027-05-25 12:15:27','2023-08-15 12:15:27'),(4,'Sports & Outdoors','2019-11-14 14:50:38','2023-08-15 14:50:38'),(5,'Books & Media','2027-04-19 16:35:51','2023-08-15 16:35:51'),(6,'Toys & Games','2028-03-22 18:22:04','2023-08-15 18:22:04'),(7,'Health & Beauty','2018-12-20 20:10:19','2023-08-15 20:10:19'),(8,'Automotive','2019-09-22 22:45:32','2023-08-15 22:45:32'),(9,'Furniture','2027-08-16 00:30:47','2023-08-16 00:30:47'),(10,'Jewelry & Watches','2018-07-16 02:14:59','2023-08-16 02:14:59');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cities`
--

DROP TABLE IF EXISTS `cities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text,
  `country_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cities`
--

LOCK TABLES `cities` WRITE;
/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
INSERT INTO `cities` VALUES (1,'New York',1,'2022-01-15 12:00:00','2022-01-15 12:00:00'),(2,'Los Angeles',1,'2022-01-16 10:30:00','2022-01-16 10:30:00'),(3,'Chicago',1,'2022-01-17 08:45:00','2022-01-17 08:45:00'),(4,'San Francisco',1,'2022-01-18 15:20:00','2022-01-18 15:20:00'),(5,'Las Vegas',1,'2022-01-19 14:10:00','2022-01-19 14:10:00'),(6,'Miami',1,'2022-01-20 11:55:00','2022-01-20 11:55:00'),(7,'Dallas',1,'2022-01-21 17:30:00','2022-01-21 17:30:00'),(8,'Boston',1,'2022-01-22 13:40:00','2022-01-22 13:40:00'),(9,'Seattle',1,'2022-01-23 16:15:00','2022-01-23 16:15:00'),(10,'Philadelphia',1,'2022-01-24 09:20:00','2022-01-24 09:20:00'),(11,'London',2,'2022-01-25 08:00:00','2022-01-25 08:00:00'),(12,'Manchester',2,'2022-01-26 12:45:00','2022-01-26 12:45:00'),(13,'Birmingham',2,'2022-01-27 15:30:00','2022-01-27 15:30:00'),(14,'Liverpool',2,'2022-01-28 10:25:00','2022-01-28 10:25:00'),(15,'Glasgow',2,'2022-01-29 14:40:00','2022-01-29 14:40:00'),(16,'Edinburgh',2,'2022-01-30 16:10:00','2022-01-30 16:10:00'),(17,'Cardiff',2,'2022-01-31 13:15:00','2022-01-31 13:15:00'),(18,'Bristol',2,'2022-02-01 09:45:00','2022-02-01 09:45:00'),(19,'Leeds',2,'2022-02-02 12:20:00','2022-02-02 12:20:00'),(20,'Newcastle',2,'2022-02-03 14:05:00','2022-02-03 14:05:00'),(21,'Berlin',3,'2022-02-04 11:55:00','2022-02-04 11:55:00'),(22,'Munich',3,'2022-02-05 09:10:00','2022-02-05 09:10:00'),(23,'Hamburg',3,'2022-02-06 08:30:00','2022-02-06 08:30:00'),(24,'Frankfurt',3,'2022-02-07 12:40:00','2022-02-07 12:40:00'),(25,'Cologne',3,'2022-02-08 14:15:00','2022-02-08 14:15:00'),(26,'Stuttgart',3,'2022-02-09 15:20:00','2022-02-09 15:20:00'),(27,'Dusseldorf',3,'2022-02-10 10:30:00','2022-02-10 10:30:00'),(28,'Leipzig',3,'2022-02-11 17:25:00','2022-02-11 17:25:00'),(29,'Dresden',3,'2022-02-12 13:40:00','2022-02-12 13:40:00'),(30,'Hanover',3,'2022-02-13 14:55:00','2022-02-13 14:55:00'),(31,'Paris',4,'2022-02-14 16:30:00','2022-02-14 16:30:00'),(32,'Marseille',4,'2022-02-15 09:25:00','2022-02-15 09:25:00'),(33,'Lyon',4,'2022-02-16 11:50:00','2022-02-16 11:50:00'),(34,'Toulouse',4,'2022-02-17 10:10:00','2022-02-17 10:10:00'),(35,'Nice',4,'2022-02-18 08:15:00','2022-02-18 08:15:00'),(36,'Bordeaux',4,'2022-02-19 14:45:00','2022-02-19 14:45:00'),(37,'Nantes',4,'2022-02-20 17:20:00','2022-02-20 17:20:00'),(38,'Lille',4,'2022-02-21 12:30:00','2022-02-21 12:30:00'),(39,'Strasbourg',4,'2022-02-22 13:45:00','2022-02-22 13:45:00'),(40,'Rennes',4,'2022-02-23 16:40:00','2022-02-23 16:40:00'),(41,'Madrid',5,'2022-02-24 15:05:00','2022-02-24 15:05:00'),(42,'Barcelona',5,'2022-02-25 10:35:00','2022-02-25 10:35:00'),(43,'Valencia',5,'2022-02-26 09:15:00','2022-02-26 09:15:00'),(44,'Seville',5,'2022-02-27 08:40:00','2022-02-27 08:40:00'),(45,'Zaragoza',5,'2022-02-28 11:00:00','2022-02-28 11:00:00'),(46,'Bilbao',5,'2022-02-28 14:10:00','2022-02-28 14:10:00'),(47,'Mallorca',5,'2022-03-01 16:25:00','2022-03-01 16:25:00'),(48,'Granada',5,'2022-03-02 12:20:00','2022-03-02 12:20:00'),(49,'Toledo',5,'2022-03-03 14:35:00','2022-03-03 14:35:00'),(50,'Murcia',5,'2022-03-04 10:15:00','2022-03-04 10:15:00');
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `countries`
--

DROP TABLE IF EXISTS `countries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `countries` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `countries`
--

LOCK TABLES `countries` WRITE;
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` VALUES (1,'United States','2022-01-15 12:00:00','2022-01-15 12:00:00'),(2,'United Kingdom','2022-01-16 10:30:00','2022-01-16 10:30:00'),(3,'Germany','2022-01-17 08:45:00','2022-01-17 08:45:00'),(4,'France','2022-01-18 15:20:00','2022-01-18 15:20:00'),(5,'Spain','2022-01-19 14:10:00','2022-01-19 14:10:00');
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_ratings`
--

DROP TABLE IF EXISTS `product_ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_ratings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `buyer_id` int DEFAULT NULL,
  `rating` enum('1','2','3','4','5') DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `buyer_id` (`buyer_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_ratings_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `product_ratings_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_ratings`
--

LOCK TABLES `product_ratings` WRITE;
/*!40000 ALTER TABLE `product_ratings` DISABLE KEYS */;
INSERT INTO `product_ratings` VALUES (1,1,33,'5','2023-10-17 19:06:50'),(2,2,33,'3','2023-11-04 03:32:08'),(3,5,34,'1','2023-11-06 03:42:16'),(4,6,25,'4','2023-10-09 10:54:51'),(5,8,26,'4','2023-10-31 04:00:49'),(6,9,32,'4','2023-09-03 02:49:45'),(7,11,21,'1','2023-10-13 21:13:52'),(8,13,23,'1','2023-11-05 21:13:58'),(9,15,30,'2','2023-09-12 20:21:32'),(10,16,26,'3','2023-10-21 00:08:35'),(11,18,31,'4','2023-11-07 02:00:13'),(12,19,24,'5','2023-08-12 21:18:16'),(13,21,34,'1','2023-09-13 10:05:01'),(14,23,38,'2','2023-10-31 14:55:29'),(15,24,22,'2','2023-11-06 13:54:56'),(16,26,36,'1','2023-08-17 23:09:12'),(17,28,25,'4','2023-11-05 20:28:53'),(18,30,32,'4','2023-09-02 23:01:34'),(19,31,29,'3','2023-09-12 14:20:51'),(20,34,22,'3','2023-11-05 00:25:32'),(21,36,35,'4','2023-10-30 20:45:04'),(22,38,29,'3','2023-11-01 21:40:28'),(23,39,27,'3','2023-10-26 16:41:36'),(24,42,34,'2','2023-09-28 01:15:19'),(25,44,24,'3','2023-11-01 02:41:42'),(26,45,38,'4','2023-10-22 02:20:06'),(27,48,21,'3','2023-10-29 09:23:17'),(28,49,34,'4','2023-10-11 12:06:11'),(29,51,34,'4','2023-10-24 13:51:04'),(30,52,34,'2','2023-08-01 21:31:07'),(31,54,30,'1','2023-10-09 02:11:13'),(32,55,38,'4','2023-08-14 22:51:33'),(33,57,33,'3','2023-08-30 06:48:50'),(34,59,27,'3','2023-07-17 11:38:18'),(35,60,26,'4','2023-11-06 09:42:49'),(36,62,32,'1','2023-10-22 06:11:37'),(37,63,36,'1','2023-09-10 07:02:42'),(38,65,38,'5','2023-10-05 03:02:57'),(39,67,25,'4','2023-10-26 06:30:42'),(40,68,23,'4','2023-10-04 10:07:15'),(41,70,31,'4','2023-11-02 03:46:43'),(42,71,30,'4','2023-10-29 15:54:03'),(43,73,28,'4','2023-10-10 03:40:49'),(44,75,24,'1','2023-10-28 06:24:58'),(45,76,25,'3','2023-07-27 10:32:15'),(46,77,35,'5','2023-09-17 18:08:27'),(47,78,24,'1','2023-10-31 23:33:58'),(48,80,22,'2','2023-09-26 05:43:49'),(49,82,40,'4','2023-11-02 21:22:40'),(50,83,37,'2','2023-09-30 04:58:29'),(51,84,31,'3','2023-10-31 20:35:59'),(52,85,39,'3','2023-10-17 08:19:04'),(53,87,32,'3','2023-11-03 19:31:52'),(54,89,34,'3','2023-07-07 01:34:51'),(55,90,27,'3','2023-08-25 03:45:25'),(56,91,21,'3','2023-10-16 14:37:15'),(57,93,28,'5','2023-10-13 21:54:08'),(58,94,30,'3','2023-10-05 20:14:01'),(59,96,21,'2','2023-09-28 22:41:42'),(60,98,29,'1','2023-10-11 19:09:53'),(61,99,36,'3','2023-07-19 22:47:49'),(62,100,31,'2','2023-11-01 15:33:04');
/*!40000 ALTER TABLE `product_ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text,
  `description` text,
  `category_id` int DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `is_sold` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Smartphone','Latest smartphone with high-resolution camera.',1,699.00,1,1,'2023-08-28 08:45:00','2023-08-28 09:45:00'),(2,'Laptop','High-performance laptop for work and play.',1,999.00,1,1,'2023-08-27 09:45:00','2023-08-27 10:45:00'),(3,'Smart TV','4K Smart TV for stunning entertainment.',1,799.00,1,0,'2023-08-26 10:45:00','2023-08-26 11:45:00'),(4,'Wireless Earbuds','Wireless earbuds for on-the-go music.',1,129.00,1,0,'2023-08-25 11:45:00','2023-08-25 12:45:00'),(5,'Tablet','Portable tablet for productivity and entertainment.',1,349.00,1,1,'2023-08-24 12:45:00','2023-08-24 13:45:00'),(6,'Designer Dress','Elegant designer dress for special occasions.',2,399.00,1,1,'2023-08-23 13:45:00','2023-08-23 14:45:00'),(7,'Running Shoes','High-performance running shoes for athletes.',2,129.00,1,0,'2023-08-22 14:45:00','2023-08-22 15:45:00'),(8,'Leather Jacket','Stylish leather jacket for a timeless look.',2,249.00,1,1,'2023-08-21 15:45:00','2023-08-21 16:45:00'),(9,'Formal Suit','Tailored formal suit for a sharp appearance.',2,299.00,1,1,'2023-08-20 16:45:00','2023-08-20 17:45:00'),(10,'Casual Jeans','Comfortable and stylish jeans for everyday wear.',2,59.00,1,0,'2023-08-19 17:45:00','2023-08-19 18:45:00'),(11,'Garden Furniture Set','Outdoor garden furniture for relaxation.',3,399.00,2,1,'2023-08-18 18:45:00','2023-08-18 19:45:00'),(12,'Barbecue Grill','High-quality barbecue grill for outdoor cooking.',3,299.00,2,0,'2023-08-17 19:45:00','2023-08-17 20:45:00'),(13,'Patio Umbrella','Patio umbrella for shade and comfort.',3,79.00,2,1,'2023-08-16 20:45:00','2023-08-16 21:45:00'),(14,'Gardening Tools Set','Complete gardening tools set for your garden.',3,49.00,2,0,'2023-08-15 21:45:00','2023-08-15 22:45:00'),(15,'Outdoor Hammock','Relaxing outdoor hammock for leisure.',3,69.00,2,1,'2023-08-14 22:45:00','2023-08-14 23:45:00'),(16,'Mountain Bike','High-performance mountain bike for off-road adventures.',4,499.00,2,1,'2023-08-13 23:45:00','2023-08-14 00:45:00'),(17,'Tennis Racket','Professional tennis racket for competitive play.',4,119.00,2,0,'2023-08-12 00:45:00','2023-08-12 01:45:00'),(18,'Soccer Ball','Official soccer ball for practice and matches.',4,29.00,2,1,'2023-08-11 01:45:00','2023-08-11 02:45:00'),(19,'Gym Equipment Set','Complete gym equipment set for workouts.',4,699.00,2,1,'2023-08-10 02:45:00','2023-08-10 03:45:00'),(20,'Camping Gear Kit','Comprehensive camping gear kit for outdoor enthusiasts.',4,199.00,2,0,'2023-08-09 03:45:00','2023-08-09 04:45:00'),(21,'Headphones','Premium over-ear headphones for immersive audio.',1,149.00,3,1,'2023-08-08 04:45:00','2023-08-08 05:45:00'),(22,'Fitness Tracker','Fitness tracker for monitoring health and workouts.',1,79.00,3,0,'2023-08-07 05:45:00','2023-08-07 06:45:00'),(23,'Camera Drone','Camera drone for aerial photography and videography.',1,299.00,3,1,'2023-08-06 06:45:00','2023-08-06 07:45:00'),(24,'Smartwatch','Smartwatch with fitness and communication features.',1,129.00,3,1,'2023-08-05 07:45:00','2023-08-05 08:45:00'),(25,'Drone with Camera','Quadcopter drone with high-definition camera.',1,289.00,4,0,'2023-08-04 08:45:00','2023-08-04 09:45:00'),(26,'Gaming Laptop','Powerful gaming laptop for immersive gaming experiences.',1,1199.00,4,1,'2023-08-03 09:45:00','2023-08-03 10:45:00'),(27,'Bluetooth Speaker','Portable Bluetooth speaker for outdoor adventures.',6,79.00,4,0,'2023-08-02 10:45:00','2023-08-02 11:45:00'),(28,'Electric Skateboard','Electric skateboard for fun and commuting.',4,499.00,4,1,'2023-08-01 11:45:00','2023-08-01 12:45:00'),(29,'Camera Lens Kit','High-quality camera lens kit for photography enthusiasts.',1,199.00,4,0,'2023-07-31 12:45:00','2023-07-31 13:45:00'),(30,'Home Gym Equipment','Compact home gym equipment for versatile workouts.',7,399.00,4,1,'2023-07-30 13:45:00','2023-07-30 14:45:00'),(31,'Smart Refrigerator','Smart refrigerator with advanced features for your kitchen.',3,1499.00,4,1,'2023-07-29 14:45:00','2023-07-29 15:45:00'),(32,'Beach Umbrella','Beach umbrella for sun protection during beach trips.',3,39.00,4,0,'2023-07-28 15:45:00','2023-07-28 16:45:00'),(33,'Digital Camera','High-resolution digital camera for photography.',1,599.00,5,0,'2023-07-27 16:45:00','2023-07-27 17:45:00'),(34,'Home Theater System','Complete home theater system for cinematic entertainment.',3,799.00,5,1,'2023-07-26 17:45:00','2023-07-26 18:45:00'),(35,'Mountain Bike','Mountain bike for outdoor adventures and trails.',4,349.00,5,0,'2023-07-25 18:45:00','2023-07-25 19:45:00'),(36,'Coffee Maker','Coffee maker for brewing delicious coffee at home.',3,69.00,5,1,'2023-07-24 19:45:00','2023-07-24 20:45:00'),(37,'Wireless Headphones','Wireless headphones for music and calls on the go.',1,99.00,5,0,'2023-07-23 20:45:00','2023-07-23 21:45:00'),(38,'Smart Thermostat','Smart thermostat for energy-efficient heating and cooling.',3,129.00,5,1,'2023-07-22 21:45:00','2023-07-22 22:45:00'),(39,'Dumbbell Set','Adjustable dumbbell set for home workouts.',4,149.00,5,1,'2023-07-21 22:45:00','2023-07-21 23:45:00'),(40,'Bluetooth Earphones','Bluetooth earphones with noise-cancellation.',1,59.00,5,0,'2023-07-20 23:45:00','2023-07-21 00:45:00'),(41,'4K Smart TV','High-end 4K Smart TV with vibrant colors.',1,1299.99,4,0,'2023-07-19 01:45:00','2023-07-19 02:45:00'),(42,'Professional Camera','Professional camera for photography and videography.',1,1999.99,4,1,'2023-07-18 02:45:00','2023-07-18 03:45:00'),(43,'Home Theater System','Immersive home theater system with surround sound.',3,899.99,4,0,'2023-07-17 03:45:00','2023-07-17 04:45:00'),(44,'Electric Scooter','Electric scooter for eco-friendly urban commuting.',4,449.99,4,1,'2023-07-16 04:45:00','2023-07-16 05:45:00'),(45,'Coffee Maker','Coffee maker with built-in grinder for fresh brews.',3,89.99,4,1,'2023-07-15 05:45:00','2023-07-15 06:45:00'),(46,'Smart Home Security System','Smart home security system with cameras and sensors.',3,349.99,4,0,'2023-07-14 06:45:00','2023-07-14 07:45:00'),(47,'Pro Gaming Chair','Professional gaming chair for long gaming sessions.',2,199.99,4,0,'2023-07-13 07:45:00','2023-07-13 08:45:00'),(48,'Dumbbell Set','Adjustable dumbbell set for versatile home workouts.',4,199.99,4,1,'2023-07-12 08:45:00','2023-07-12 09:45:00'),(49,'Bluetooth Headphones','Wireless Bluetooth headphones for music enthusiasts.',1,79.99,5,1,'2023-07-11 09:45:00','2023-07-11 10:45:00'),(50,'Robotic Vacuum Cleaner','Smart robotic vacuum cleaner for automated cleaning.',3,349.99,5,0,'2023-07-10 10:45:00','2023-07-10 11:45:00'),(51,'Hybrid Bike','Hybrid bike for versatile on-road and off-road riding.',4,599.99,5,1,'2023-07-09 11:45:00','2023-07-09 12:45:00'),(52,'Instant Pot','Multi-functional Instant Pot for easy cooking.',3,89.99,5,1,'2023-07-08 12:45:00','2023-07-08 13:45:00'),(53,'Noise-Canceling Headphones','Noise-canceling headphones for peace and quiet.',1,129.99,5,0,'2023-07-07 13:45:00','2023-07-07 14:45:00'),(54,'Smart Thermostat','Smart thermostat for energy-efficient climate control.',3,119.99,5,1,'2023-07-06 14:45:00','2023-07-06 15:45:00'),(55,'Yoga Mat','Premium yoga mat for comfortable workouts.',4,19.99,5,1,'2023-07-05 15:45:00','2023-07-05 16:45:00'),(56,'Wireless Mouse','Wireless mouse for seamless computer navigation.',1,29.99,5,0,'2023-07-04 16:45:00','2023-07-04 17:45:00'),(57,'Samsung Galaxy S21 Ultra','Flagship smartphone with top-notch camera and 5G connectivity.',1,799.99,12,1,'2023-06-06 21:45:00','2023-06-06 22:45:00'),(58,'Dell XPS 15 Laptop','Powerful laptop with 4K OLED display for professionals.',1,1199.95,12,0,'2023-06-05 22:45:00','2023-06-05 23:45:00'),(59,'Sony WH-1000XM4 Headphones','Industry-leading noise-canceling headphones for audiophiles.',1,89.95,12,1,'2023-06-04 23:45:00','2023-06-05 00:45:00'),(60,'LG 65-inch 4K Smart TV','Large 4K Smart TV with webOS for smart entertainment.',1,999.75,12,1,'2023-06-04 00:45:00','2023-06-04 01:45:00'),(61,'PlayStation 5','The latest PlayStation console for gaming enthusiasts.',6,349.50,12,0,'2023-06-03 01:45:00','2023-06-03 02:45:00'),(62,'Logitech MX Master 3 Mouse','Wireless mouse with advanced features for productivity.',1,29.75,12,1,'2023-06-03 02:45:00','2023-06-03 03:45:00'),(63,'Bose QuietComfort 45 Headphones','Premium noise-canceling headphones for music lovers.',1,69.99,12,1,'2023-06-03 03:45:00','2023-06-03 04:45:00'),(64,'Canon EOS 90D DSLR Camera','High-quality DSLR camera for photography and videography.',1,499.95,12,0,'2023-06-03 04:45:00','2023-06-03 05:45:00'),(65,'Apple AirPods Pro','Wireless earbuds with active noise cancellation for immersive audio.',1,59.75,13,1,'2023-06-03 05:45:00','2023-06-03 06:45:00'),(66,'Amazon Echo Studio','High-fidelity smart speaker with 3D audio for home entertainment.',3,99.95,13,0,'2023-06-03 06:45:00','2023-06-03 07:45:00'),(67,'Fitbit Charge 5','Advanced fitness tracker with built-in GPS and heart rate monitoring.',1,79.95,13,1,'2023-06-03 07:45:00','2023-06-03 08:45:00'),(68,'DJI Air 2S Drone','High-performance drone with 5.4K video and obstacle sensors.',1,299.50,13,1,'2023-06-03 08:45:00','2023-06-03 09:45:00'),(69,'Garmin Forerunner 945','Multisport smartwatch with advanced fitness and health features.',1,129.99,13,0,'2023-06-03 09:45:00','2023-06-03 10:45:00'),(70,'Rustic Wooden Coffee Table','Rustic wooden coffee table for a warm and inviting living room.',9,249.99,14,1,'2023-06-04 17:45:00','2023-06-04 18:45:00'),(71,'Adventure Travel Guidebook','Comprehensive guidebook for adventurous travelers.',5,29.95,14,1,'2023-06-04 18:45:00','2023-06-04 19:45:00'),(72,'Elegant Diamond Necklace','Elegant diamond necklace with a stunning pendant.',10,999.95,14,0,'2023-06-04 19:45:00','2023-06-04 20:45:00'),(73,'Camping and Hiking Gear Set','Complete camping and hiking gear set for outdoor enthusiasts.',4,349.75,14,1,'2023-06-04 20:45:00','2023-06-04 21:45:00'),(74,'Vintage Book Collection','Collection of vintage books from various genres.',5,149.50,14,0,'2023-06-04 21:45:00','2023-06-04 22:45:00'),(75,'Luxury Wristwatch','Luxury wristwatch with a timeless design and premium materials.',10,699.99,14,1,'2023-06-04 22:45:00','2023-06-04 23:45:00'),(76,'Outdoor Camping Tent','Spacious outdoor camping tent for a comfortable camping experience.',4,199.99,14,1,'2023-06-04 23:45:00','2023-06-05 00:45:00'),(77,'Mid-Century Modern Sofa','Stylish mid-century modern sofa for a retro-inspired living room.',9,899.99,15,1,'2023-06-05 00:45:00','2023-06-05 01:45:00'),(78,'Classic Literature Collection','Collection of classic literature books by renowned authors.',5,99.95,15,1,'2023-06-05 01:45:00','2023-06-05 02:45:00'),(79,'Diamond Tennis Bracelet','Elegant diamond tennis bracelet with dazzling stones.',10,110.20,15,0,'2023-06-05 02:45:00','2023-06-05 03:45:00'),(80,'Camping Cookware Set','Compact and durable camping cookware set for outdoor cooking.',4,79.50,15,1,'2023-06-05 03:45:00','2023-06-05 04:45:00'),(81,'Science Fiction Book Series','Popular science fiction book series with thrilling adventures.',5,40.20,15,0,'2023-06-05 04:45:00','2023-06-05 05:45:00'),(82,'Luxury Gold Necklace','Exquisite luxury gold necklace with intricate details and fine craftsmanship.',10,350.00,15,1,'2023-06-05 05:45:00','2023-06-05 06:45:00'),(83,'Backpacking Tent and Gear','Complete backpacking tent and gear kit for outdoor adventures.',4,210.90,15,1,'2023-06-05 06:45:00','2023-06-05 07:45:00'),(84,'Smart Home Security System','Comprehensive smart home security system with cameras and sensors.',1,349.99,18,1,'2023-06-05 07:45:00','2023-06-05 08:45:00'),(85,'Vintage Vinyl Record Player','Vintage-style vinyl record player for music enthusiasts.',6,149.95,18,1,'2023-06-05 08:45:00','2023-06-05 09:45:00'),(86,'Designer Leather Wallet','Designer leather wallet with multiple card slots and a sleek design.',10,89.75,18,0,'2023-06-05 09:45:00','2023-06-05 10:45:00'),(87,'Camping and Outdoor Cooking Set','Versatile cooking set for outdoor adventures and camping trips.',4,59.50,18,1,'2023-06-05 10:45:00','2023-06-05 11:45:00'),(88,'Art History Books Collection','Collection of art history books covering various art movements.',5,79.99,18,0,'2023-06-05 11:45:00','2023-06-05 12:45:00'),(89,'Luxury Diamond Necklace','Exquisite diamond necklace with a timeless design and brilliant stones.',10,1599.99,18,1,'2023-06-05 12:45:00','2023-06-05 13:45:00'),(90,'Family Camping Tent','Spacious family camping tent with separate sleeping areas.',4,199.99,18,1,'2023-06-05 13:45:00','2023-06-05 14:45:00'),(91,'Cooking and Baking Essentials','Essential cooking and baking tools for the home kitchen.',3,129.50,18,1,'2023-06-05 14:45:00','2023-06-05 15:45:00'),(92,'Antique Pocket Watch','Antique pocket watch with intricate engravings and a vintage charm.',10,399.99,18,0,'2023-06-05 15:45:00','2023-06-05 16:45:00'),(93,'Outdoor Adventure Backpack','Durable outdoor adventure backpack for hiking and camping.',4,69.99,19,1,'2023-06-05 16:45:00','2023-06-05 17:45:00'),(94,'Vintage Vinyl Record Collection','Collection of vintage vinyl records with classic music.',6,129.95,19,1,'2023-06-05 17:45:00','2023-06-05 18:45:00'),(95,'Elegant Silver Earrings','Elegant silver earrings with a unique and modern design.',10,59.75,19,0,'2023-06-05 18:45:00','2023-06-05 19:45:00'),(96,'Camping Cookware and Utensils','Compact camping cookware and utensils set for outdoor cooking.',4,49.50,19,1,'2023-06-05 19:45:00','2023-06-05 20:45:00'),(97,'Classic Literature Collection','Collection of classic literature books by renowned authors.',5,99.99,19,0,'2023-06-05 20:45:00','2023-06-05 21:45:00'),(98,'Luxury Sapphire Ring','Luxury sapphire ring with a stunning blue sapphire gemstone.',10,349.99,19,1,'2023-06-05 21:45:00','2023-06-05 22:45:00'),(99,'Family Camping Tent','Spacious family camping tent with separate sleeping areas.',4,199.99,19,1,'2023-06-05 22:45:00','2023-06-05 23:45:00'),(100,'Baking and Pastry Tools','Essential baking and pastry tools for the home kitchen.',3,79.50,19,1,'2023-06-05 23:45:00','2023-06-06 00:45:00');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `seller_id` int DEFAULT NULL,
  `buyer_id` int DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `seller_id` (`seller_id`),
  KEY `buyer_id` (`buyer_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `transactions_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,1,33,699.00,'2023-09-02 22:40:18'),(2,2,1,33,999.00,'2023-10-25 17:08:11'),(3,5,1,34,349.00,'2023-10-07 18:54:45'),(4,6,1,25,399.00,'2023-09-06 05:46:30'),(5,8,1,26,249.00,'2023-10-23 08:44:08'),(6,9,1,32,299.00,'2023-08-30 16:01:48'),(7,11,2,21,399.00,'2023-10-04 04:19:18'),(8,13,2,23,79.00,'2023-10-14 14:40:18'),(9,15,2,30,69.00,'2023-08-26 16:31:38'),(10,16,2,26,499.00,'2023-10-19 22:30:54'),(11,18,2,31,29.00,'2023-10-25 16:39:43'),(12,19,2,24,699.00,'2023-08-12 14:44:05'),(13,21,3,34,149.00,'2023-08-22 10:38:30'),(14,23,3,38,299.00,'2023-10-27 03:37:47'),(15,24,3,22,129.00,'2023-09-27 19:15:40'),(16,26,4,36,1199.00,'2023-08-14 15:56:09'),(17,28,4,25,499.00,'2023-10-18 10:51:55'),(18,30,4,32,399.00,'2023-08-20 03:08:58'),(19,31,4,29,1499.00,'2023-09-11 20:35:30'),(20,34,5,22,799.00,'2023-10-30 23:35:38'),(21,36,5,35,69.00,'2023-10-04 18:12:23'),(22,38,5,29,129.00,'2023-10-23 07:21:54'),(23,39,5,27,149.00,'2023-10-25 11:14:00'),(24,42,4,34,1999.99,'2023-09-24 11:00:29'),(25,44,4,24,449.99,'2023-10-17 11:03:15'),(26,45,4,38,89.99,'2023-10-01 13:39:43'),(27,48,4,21,199.99,'2023-10-25 16:00:40'),(28,49,5,34,79.99,'2023-09-09 21:49:43'),(29,51,5,34,599.99,'2023-09-30 22:26:20'),(30,52,5,34,89.99,'2023-07-18 14:26:58'),(31,54,5,30,119.99,'2023-07-13 01:27:50'),(32,55,5,38,19.99,'2023-08-05 05:35:33'),(33,57,12,33,799.99,'2023-08-12 00:02:26'),(34,59,12,27,89.95,'2023-07-12 02:33:34'),(35,60,12,26,999.75,'2023-09-15 06:09:02'),(36,62,12,32,29.75,'2023-10-17 07:39:42'),(37,63,12,36,69.99,'2023-06-07 13:29:30'),(38,65,13,38,59.75,'2023-07-30 04:03:18'),(39,67,13,25,79.95,'2023-10-24 16:59:09'),(40,68,13,23,299.50,'2023-09-28 02:03:16'),(41,70,14,31,249.99,'2023-07-04 05:58:16'),(42,71,14,30,29.95,'2023-09-25 15:07:21'),(43,73,14,28,349.75,'2023-09-12 19:17:02'),(44,75,14,24,699.99,'2023-10-25 23:04:07'),(45,76,14,25,199.99,'2023-07-16 15:51:59'),(46,77,15,35,899.99,'2023-09-10 01:07:54'),(47,78,15,24,99.95,'2023-10-19 16:19:16'),(48,80,15,22,79.50,'2023-09-08 13:01:51'),(49,82,15,40,350.00,'2023-10-23 04:55:36'),(50,83,15,37,210.90,'2023-07-03 12:00:22'),(51,84,18,31,349.99,'2023-06-20 17:41:57'),(52,85,18,39,149.95,'2023-07-14 07:56:17'),(53,87,18,32,59.50,'2023-06-16 13:34:12'),(54,89,18,34,1599.99,'2023-06-13 19:31:52'),(55,90,18,27,199.99,'2023-08-01 22:10:48'),(56,91,18,21,129.50,'2023-10-14 04:00:44'),(57,93,19,28,69.99,'2023-07-24 03:27:51'),(58,94,19,30,129.95,'2023-07-16 12:07:39'),(59,96,19,21,49.50,'2023-07-19 22:21:36'),(60,98,19,29,349.99,'2023-07-05 17:46:15'),(61,99,19,36,199.99,'2023-06-28 22:38:55'),(62,100,19,31,79.50,'2023-06-27 10:03:54');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text,
  `lastname` text,
  `email` text,
  `password` text,
  `birthdate` date DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `city_id` int DEFAULT NULL,
  `country_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `city_id` (`city_id`),
  KEY `country_id` (`country_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`),
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`country_id`) REFERENCES `countries` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'John','Doe','john.doe@example.com','password123','1990-05-15',1,1,1,'2022-01-15 12:00:00','2023-08-15 09:23:45'),(2,'Jane','Smith','jane.smith@example.com','password456','1985-12-10',1,1,1,'2022-02-15 13:30:00','2023-08-15 10:30:12'),(3,'Michael','Johnson','michael.j@example.com','pass789','1995-03-25',1,1,1,'2022-03-15 14:45:00','2023-08-15 12:15:27'),(4,'Emily','Brown','emily.brown@example.com','secure456','1992-11-20',1,1,1,'2022-04-15 16:20:00','2023-08-15 14:50:38'),(5,'David','Davis','david.d@example.com','secret123','1987-04-19',1,2,1,'2022-05-15 18:10:00','2023-08-15 16:35:51'),(6,'Sophia','Wilson','sophia.w@example.com','pass321','1998-01-22',1,2,1,'2022-06-15 20:05:00','2023-08-15 18:22:04'),(7,'William','Lee','william.lee@example.com','key123','1994-09-20',1,2,1,'2022-07-15 22:45:00','2023-08-15 20:10:19'),(8,'Olivia','Harris','olivia.h@example.com','pwd789','1996-08-12',1,1,1,'2022-08-15 00:30:00','2023-08-16 00:30:47'),(9,'Ethan','Martinez','ethan.m@example.com','pass456','1991-07-16',1,1,1,'2022-09-15 02:20:00','2023-08-16 02:14:59'),(10,'Ava','Gonzalez','ava.g@example.com','secure123','1993-06-24',1,11,2,'2022-10-15 04:15:00','2023-08-16 04:40:36'),(11,'Liam','Rodriguez','liam.r@example.com','password567','1990-09-05',1,11,2,'2022-11-15 06:00:00','2023-08-16 06:55:28'),(12,'Mia','Lopez','mia.l@example.com','pass123','1991-04-30',1,11,2,'2022-12-15 08:30:00','2023-08-16 08:30:00'),(13,'James','Turner','james.t@example.com','secure789','1986-03-15',1,11,2,'2023-01-15 10:45:00','2023-08-16 10:45:00'),(14,'Charlotte','Hernandez','charlotte.h@example.com','pass987','1997-11-10',1,14,2,'2023-02-15 12:20:00','2023-08-16 12:20:00'),(15,'Benjamin','Smith','benjamin.s@example.com','password654','1992-05-19',1,14,2,'2023-03-15 14:10:00','2023-08-16 14:10:00'),(16,'Avery','Williams','avery.w@example.com','secret987','1989-02-22',1,14,2,'2023-04-15 16:05:00','2023-08-16 16:05:00'),(17,'Henry','Brown','henry.b@example.com','pass321','1994-10-20',1,14,2,'2023-05-15 18:25:00','2023-08-16 18:25:00'),(18,'Amelia','Davis','amelia.d@example.com','key456','1988-08-12',1,14,2,'2023-06-15 20:40:00','2023-08-16 20:40:00'),(19,'Sebastian','Garcia','sebastian.g@example.com','secure321','1998-07-16',1,14,2,'2023-07-15 22:35:00','2023-08-16 22:35:00'),(20,'Ella','Perez','ella.p@example.com','pwd987','1993-06-24',1,21,3,'2023-08-15 00:15:00','2023-08-17 00:15:00'),(21,'Jackson','Martinez','jackson.m@example.net','pass123','1984-09-25',1,21,3,'2020-01-15 06:00:00','2023-08-16 06:55:28'),(22,'Luna','Turner','luna.t@example.net','secure789','1980-04-30',1,21,3,'2020-02-15 08:30:00','2023-08-16 08:30:00'),(23,'Samuel','Hernandez','samuel.h@example.org','pass987','1976-03-15',1,21,3,'2020-03-15 10:45:00','2023-08-16 10:45:00'),(24,'Penelope','Smith','penelope.s@example.org','password654','1977-11-10',1,21,3,'2020-04-15 12:20:00','2023-08-16 12:00:00'),(25,'Luke','Williams','luke.w@example.net','secret987','1971-02-22',1,21,3,'2020-05-15 14:10:00','2023-08-16 14:10:00'),(26,'Evelyn','Brown','evelyn.b@example.org','pass321','1975-10-20',1,30,3,'2020-06-15 16:05:00','2023-08-16 16:05:00'),(27,'Daniel','Davis','daniel.d@example.net','key456','1968-08-12',1,30,3,'2020-07-15 18:25:00','2023-08-16 18:25:00'),(28,'Victoria','Garcia','victoria.g@example.org','secure321','1967-07-16',1,31,4,'2020-08-15 20:40:00','2023-08-16 20:40:00'),(29,'William','Perez','william.p@example.net','pwd987','1970-06-24',1,31,4,'2020-09-15 22:35:00','2023-08-16 22:35:00'),(30,'Aria','Lopez','aria.l@example.org','secure123','1963-07-19',1,31,4,'2020-10-15 00:15:00','2023-08-17 00:15:00'),(31,'Charlie','Smith','charlie.smith@fake.com','password123','1990-09-05',1,31,4,'2018-01-15 06:00:00','2023-08-16 06:55:28'),(32,'Lucy','Johnson','lucy.johnson@fake.com','secure789','1985-04-30',1,33,4,'2018-02-15 08:30:00','2023-08-16 08:30:00'),(33,'Max','Williams','max.williams@fake.com','pass987','1995-03-15',1,33,4,'2018-03-15 10:45:00','2023-08-16 10:45:00'),(34,'Lily','Brown','lily.brown@fake.com','password654','1992-11-10',1,33,4,'2018-04-15 12:20:00','2023-08-16 12:20:00'),(35,'Chuck','Davis','chuck.davis@fake.com','secret987','1987-04-19',1,33,4,'2018-05-15 14:10:00','2023-08-16 14:10:00'),(36,'Maddie','Wilson','maddie.wilson@fake.com','pass321','1998-01-22',1,41,5,'2018-06-15 16:05:00','2023-08-16 16:05:00'),(37,'Ziggy','Garcia','ziggy.garcia@fake.com','key456','1994-09-20',1,41,5,'2018-07-15 18:25:00','2023-08-16 18:25:00'),(38,'Jokesy','Lopez','jokesy.lopez@fake.com','secure321','1996-08-12',1,44,5,'2018-08-15 20:40:00','2023-08-16 20:40:00'),(39,'Giggle','Perez','giggle.perez@fake.com','pwd987','1991-07-16',1,44,5,'2018-09-15 22:35:00','2023-08-16 22:35:00'),(40,'Snicker','Gonzalez','snicker.gonzalez@fake.com','secure123','1993-06-24',1,44,5,'2018-10-15 00:15:00','2023-08-17 00:15:00'),(41,'Milo','Jackson','milo.jackson@gmail-invent.dev','password123','1994-02-15',0,50,5,'2018-11-15 04:00:00','2023-08-18 06:55:28'),(42,'Zoe','Sanchez','zoe.sanchez@gmail-invent.dev','secure789','1989-06-30',0,50,5,'2019-02-15 07:30:00','2023-08-18 09:30:12'),(43,'Leo','Fernandez','leo.fernandez@gmail-invent.dev','pass987','1990-07-15',0,50,5,'2019-03-15 10:45:00','2023-08-18 12:15:27'),(44,'Eva','Moore','eva.moore@gmail-invent.dev','password654','1985-04-10',0,50,5,'2019-04-15 14:20:00','2023-08-18 14:50:38'),(45,'Mason','Adams','mason.adams@gmail-invent.dev','secret987','1991-03-25',0,50,5,'2019-05-15 16:10:00','2023-08-18 16:35:51'),(46,'Luna','Parker','luna.parker@gmail-invent.dev','pass321','1997-12-22',0,1,1,'2019-06-15 18:05:00','2023-08-18 18:22:04'),(47,'Eli','Collins','eli.collins@gmail-invent.dev','key456','1983-09-20',0,1,1,'2019-07-15 20:45:00','2023-08-18 20:10:19'),(48,'Nora','Baker','nora.baker@gmail-invent.dev','secure321','1986-08-12',0,1,1,'2019-08-15 22:40:00','2023-08-18 22:30:47'),(49,'Theo','Gibson','theo.gibson@gmail-invent.dev','pwd987','1995-07-16',0,31,4,'2019-09-15 00:20:00','2023-08-19 02:14:59'),(50,'Hazel','Wright','hazel.wright@gmail-invent.dev','secure123','1988-06-24',1,3,1,'2019-10-15 02:15:00','2023-08-19 04:40:36');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-11-07  9:48:34
