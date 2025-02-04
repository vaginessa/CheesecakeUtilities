// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp();

// Calculate Statistics (non-training mileage only for now)
exports.calculateStatistics = functions.database.ref('/vehmileage/users/{userid}/records').onWrite(
    (snapshot, context) => {
        console.log("Function Version: 030620191912")
        console.log("Dep Versions listed below");
        console.log(process.versions)
        const records = snapshot.after.val();
        var stats = {totalMileage: 0}
        console.log('Processing User', context.params.userid);
        console.log('Calculating Total Mileage...');
        var total = calculateTotalMileage(records);
        stats.totalMileage = total;
        console.log('Total Mileage: ', total, ' km');
        console.log('Calculating Class Total Mileage...');
        // Only save those that has more than 0;
        var classM = calculateByClass(records);
        stats = Object.assign({}, stats, classM);
        console.log('Processing Mileage split by misc items (date, month, vehicle type)...');
        var misc = miscMileage(records);
        stats.timeRecords = misc.time;
        stats.vehicleTypes = misc.vehicles;
        stats.vehicleNumberRecords = misc.vehicleNumber;
        console.log('Finished Processing User. Saving to Firebase DB');
        console.log(stats);
        return setRecord(snapshot.after.ref.parent.child('statistics'), stats, context);
    }
)

function setRecord(ref, stats, context) {
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, 'app');

    const deleteApp = () => app.delete().catch(() => null);
    return app.database().ref(ref).set(stats).then(res => {
        // Deleting the app is necessary for preventing concurrency leaks
        return deleteApp().then(() => res);
      }).catch(err => {
        return deleteApp().then(() => Promise.reject(err));
      });
}

function miscMileage(recordList) {
    var date = {};
    var month = {};
    var vehicle = {};
    var vehicleNum = {};
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            // Add Date Time mileage
            var timezoneOffset = 8 * 60 * 60 * 1000; // 8 Hours
            if (recordList[key].hasOwnProperty("timezone")) timezoneOffset = recordList[key].timezone; // Adjust date to timezone
            var rDate = new Date(parseInt(recordList[key].datetimeFrom, 10) + timezoneOffset); // Init with timezone difference
            rDate.setHours(0,0,0,0); // Add to date
            if (!date[rDate.getTime().toString()]) date[rDate.getTime().toString()] = 0.0;
            date[rDate.getTime().toString()] += parseFloat(recordList[key].totalMileage);
            rDate.setDate(1); // Add to month
            if (!month[rDate.getTime().toString()]) month[rDate.getTime().toString()] = 0.0;
            month[rDate.getTime().toString()] += parseFloat(recordList[key].totalMileage);

            // Add Vehicle filtered mileage
            if (!vehicle[recordList[key].vehicleId]) vehicle[recordList[key].vehicleId] = 0.0;
            if (!vehicleNum[recordList[key].vehicleNumber]) vehicleNum[recordList[key].vehicleNumber] = 0.0;
            vehicle[recordList[key].vehicleId] += parseFloat(recordList[key].totalMileage); // Vehicle Type
            vehicleNum[recordList[key].vehicleNumber] += parseFloat(recordList[key].totalMileage); // Vehicle Number
        }
    });
    var result = {time: {perDate: date, perMonth: month}, vehicles: vehicle, vehicleNumber: vehicleNum}
    return result;
}

function calculateTotalMileage(recordList) {
    var mileage = 0.0;
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            mileage += parseFloat(recordList[key].totalMileage);
        }
    });
    return mileage;
}

function calculateByClass(recordList) {
    var classMileage = {};
    Object.keys(recordList).forEach(key => {
        if (typeof recordList[key] === 'object') {
            if (recordList[key].trainingMileage == true) return;
            if (!classMileage[recordList[key].vehicleClass]) classMileage[recordList[key].vehicleClass] = 0.0;
            classMileage[recordList[key].vehicleClass] += parseFloat(recordList[key].totalMileage);
        }
    });
    return classMileage;
}