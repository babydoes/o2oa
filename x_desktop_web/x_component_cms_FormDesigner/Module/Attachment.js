MWF.xApplication.cms.FormDesigner.Module = MWF.xApplication.cms.FormDesigner.Module || {};
MWF.xDesktop.requireApp("process.FormDesigner", "Module.Attachment", null, false);
MWF.xApplication.cms.FormDesigner.Module.Attachment = MWF.CMSFCAttachment = new Class({
	Extends: MWF.FCAttachment,
	Implements : [MWF.CMSFCMI]
});
