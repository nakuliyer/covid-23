## Overall Idea for Contact Tracing:
The mobile will can call the `get_new()` in ContactTracingSQL to get a new
code that'll then be dumped into the `used_codes` table. This should happen
multiple times a day locally. The mobile app should locally store ALL the codes
that the person has seen (ideally forever, but for minimizing storage this might be
deleted every 14 days or whatever).

When two phones are in contact with each other (i.e. 6 feet), we create an
`AnonymousContact` this holds `first_secret` (one mobile's most recent code) and
`second_secret` (the other mobile's most recent code). A `time_stamp` is recorded
and the actual distance may or may not be recorded. We add this entry to the
ct_main database.

So when one person gets the virus, ALL the codes from their local phone are added to
to the `compromised_codes` database.

And then the other mobile phones regularly check `ct_main` to check whether they've
linked one of the compromised codes. It can be extended so that we check for friends
of friends, etc.