const admin = require("firebase-admin");
const {onValueCreated} = require("firebase-functions/v2/database");

admin.initializeApp();

exports.userNotification = onValueCreated(
    "/notifications/{uid}/{notifId}",
    (event) => {
      const uid = event.params.uid;
      const data = event.data.val();

      return admin.messaging().sendToTopic("user_" + uid, {
        notification: {
          title: data.title,
          body: data.message,
        },
      });
    },
);

exports.adminNotification = onValueCreated(
    "/admin_notifications/{notifId}",
    (event) => {
      const data = event.data.val();

      return admin.messaging().sendToTopic("admin", {
        notification: {
          title: data.title,
          body: data.message,
        },
      });
    },
);
