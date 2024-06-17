package com.omega.jobservice.controller;

import com.omega.jobservice.queue.context.QueueMessage;
import com.omega.jobservice.queue.service.InstantJobsQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/instantJobs")
public class InstantJobsAction {

    @Autowired
    private InstantJobsQueueService instantJobsQueueService;

    @PostMapping("/push")
    public ResponseEntity<String> sendMessage(@RequestParam String jobName, @RequestBody Serializable serializableMessage) {
        try {
            instantJobsQueueService.sendMessage(jobName, serializableMessage);
            return ResponseEntity.ok("Message sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending message: " + e.getMessage());
        }
    }

    @GetMapping("/pull")
    public ResponseEntity<List<QueueMessage>> getMessageObjects(@RequestParam int limit) {
        try {
            List<QueueMessage> messages = instantJobsQueueService.getMessageObjects(limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/changeTransactionTimeout")
    public ResponseEntity<String> changeTransactionTimeout(@RequestParam String messageId, @RequestParam int transactionTimeout) {
        try {
            boolean result = instantJobsQueueService.changeTransactionTimeout(messageId, transactionTimeout);
            if (result) {
                return ResponseEntity.ok("Transaction timeout updated successfully.");
            } else {
                return ResponseEntity.status(404).body("Message not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating transaction timeout: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMessageObject(@RequestParam String messageId) {
        try {
            instantJobsQueueService.deleteMessageObject(messageId);
            return ResponseEntity.ok("Message deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting message: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteQueue")
    public ResponseEntity<String> deleteQueue(@RequestParam long tTime) {
        try {
            instantJobsQueueService.deleteQueue(tTime);
            return ResponseEntity.ok("Queue cleaned successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error cleaning queue: " + e.getMessage());
        }
    }
}

