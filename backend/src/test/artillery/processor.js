const currentRunMinute = Math.floor(Date.now() / 60000);
const offsetDays = (currentRunMinute % 300) + 10; 

let targetDate = new Date();
targetDate.setDate(targetDate.getDate() + offsetDays);

//avoid weekends
if (targetDate.getDay() === 0) targetDate.setDate(targetDate.getDate() + 1);
if (targetDate.getDay() === 6) targetDate.setDate(targetDate.getDate() + 2);

const year = targetDate.getFullYear();
const month = String(targetDate.getMonth() + 1).padStart(2, '0');
const day = String(targetDate.getDate()).padStart(2, '0');

const testStartDate = `${year}-${month}-${day}T10:00:00.000+02:00`;
const testEndDate = `${year}-${month}-${day}T12:00:00.000+02:00`;

module.exports = {
  setupDates: function(context, events, done) {
    //dinamic dates for artillery
    context.vars.dynamicStartDate = testStartDate;
    context.vars.dynamicEndDate = testEndDate;
    if (context.vars.email) {
      context.vars.email = context.vars.email.trim();
    }
    if (context.vars.password) {
      context.vars.password = context.vars.password.trim();
    }
    return done();
  }
};