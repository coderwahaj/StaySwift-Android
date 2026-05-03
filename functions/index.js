const admin = require("firebase-admin");
const {onValueCreated} = require("firebase-functions/v2/database");

admin.initializeApp();

exports.userNotification = onValueCreated(
  "/notifications/{uid}/{notifId}",
  async (event) => {
    const uid = event.params.uid;
    const data = event.data.val();

    return admin.messaging().send({
      topic: `user_${uid}`,
      notification: {
        title: data.title || "StaySwift",
        body: data.message || "",
      },
    });
  },
);

exports.adminNotification = onValueCreated(
  "/admin_notifications/{notifId}",
  async (event) => {
    const data = event.data.val();

    return admin.messaging().send({
      topic: "admin",
      notification: {
        title: data.title || "StaySwift",
        body: data.message || "",
      },
    });
  },
);