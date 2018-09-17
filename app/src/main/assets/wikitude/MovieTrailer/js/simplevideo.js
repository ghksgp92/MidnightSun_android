var World = {
	loaded: false,
	hasVideoStarted:false,

	init: function initFn() {
		this.createOverlays();
	},

    // 화면 위에 무언가 그릴때 사용함
	createOverlays: function createOverlaysFn() {

		/*
		인식할 타겟 정보가 있는 wtc 파일 로드
        wtc 파일은 앱 내부의 위치를 참조하거나 URL 로 참조할 수 있다.
        wtc 파일은 Wikitude Target Collection 의 준말로, witkitude target manager 를 통해 관리할 수 있다.
        */
		this.targetCollectionResource = new AR.TargetCollectionResource("http://183.111.227.218/files/trailer/trailer.wtc", {
            onError: function(errorMessage) {
            	alert(errorMessage);
            }
        });

        // wtc 파일로 이미지 추적기를 생성한다.
        this.tracker = new AR.ImageTracker(this.targetCollectionResource, {
            onTargetsLoaded: this.worldLoaded, // 타겟을 인식하면 실행할 메서드를 지정함.
            onError: function(errorMessage) {
            	alert(errorMessage);
            }
        });

		/*
			Besides images, text and HTML content you are able to display videos in augmented reality. With the help of AR.VideoDrawables you can add a video on top of any image recognition target (AR.ImageTrackable) or have it displayed at any geo location (AR.GeoObject). Like any other drawable you can position, scale, rotate and change the opacity of the video drawable.

			The video we use for this example is "video.mp4". As with all resources the video can be loaded locally from the application bundle or remotely from any server. In this example the video file is already bundled with the application.

			The URL and the size are required when creating a new AR.VideoDrawable. Optionally the offsetX and offsetY parameters are set to position the video on the target. The values for the offsets are in SDUs. If you want to know more about SDUs look up the code reference.
		*/

		/*
             Wikitude 타겟 매니저에서 타겟 이미지와 재생할 동영상을 지정할 수 있다.
             재생 가능한 동영상은 재생방식에 따라 동영상 포맷의 차이가 있다.
             자세한 내용은 https://www.wikitude.com/external/doc/documentation/latest/android/workingwithvideos.html#video-drawables 참조.
             현재 Wikitude 는 동영상을 재생가능한 영상으로 변환하기 위해 https://handbrake.fr/ 를 사용한다고 한다.

             나는 포스터 위에 재생되는 예고편을 원하기 때문에 H.264 baseline 으로 인코딩된 mp4 동영상 파일을 사용해야한다.
		*/

		/*
			Adding the video to the image target is straight forward and similar like adding any other drawable to an image target.

			Note that this time we use "*" as target name. That means that the AR.ImageTrackable will respond to any target that is defined in the target collection. You can use wildcards to specify more complex name matchings. E.g. 'target_?' to reference 'target_1' through 'target_9' or 'target*' for any targets names that start with 'target'.

			To start the video immediately after the target is recognized we call play inside the onImageRecognized trigger. Supplying -1 to play tells the Wikitude SDK to loop the video infinitely. Choose any positive number to re-play it multiple times.

			Once the video has been started for the first time (indicated by this.hasVideoStarted), we call pause every time the target is lost and resume every time the tracker is found again to continue the video where it left off.
		*/
		    /*

             Wikitude 타겟 매니저에서 타겟 이미지와 재생할 동영상을 지정할 수 있다.
             재생 가능한 동영상은 재생방식에 따라 동영상 포맷의 차이가 있다.
             자세한 내용은 https://www.wikitude.com/external/doc/documentation/latest/android/workingwithvideos.html#video-drawables 참조.
             현재 Wikitude 는 동영상을 재생가능한 영상으로 변환하기 위해 https://handbrake.fr/ 를 사용한다고 한다.

             나는 포스터 위에 재생되는 예고편을 원하기 때문에 H.264 baseline 으로 인코딩된 mp4 동영상 파일을 사용해야한다.

            */


		// 마녀
		var video175322 = new AR.VideoDrawable("http://183.111.227.218/files/trailer/175322.mp4", 0.40, {
            translate: {
                y: -0.3
            }
        });
		var tracker175322 = new AR.ImageTrackable(this.tracker, "175322", {
			drawables: {
				cam: [video175322]
			},
			onImageRecognized: function onImageRecognizedFn() {
				if (this.hasVideoStarted) {
					video175322.resume();
				}
				else {
					this.hasVideoStarted = true;
					video175322.play(-1);
				}
				World.removeLoadingBar();
			},
			onImageLost: function onImageLostFn() {
				video175322.pause();
			},
            onError: function(errorMessage) {
            	alert(errorMessage);
            }
		});

        // 아이필프리티
        var video168017 = new AR.VideoDrawable("http://183.111.227.218/files/trailer/168017.mp4", 0.40, {
                       translate: {
                           y: -0.3
                       }
                   });

           var tracker168017 = new AR.ImageTrackable(this.tracker, "168017", {
               drawables: {
                   cam: [video168017]
               },
               onImageRecognized: function onImageRecognizedFn() {
                   if (this.hasVideoStarted) {
                       video168017.resume();
                   }
                   else {
                       this.hasVideoStarted = true;
                       video168017.play(-1);
                   }
                   World.removeLoadingBar();
               },
               onImageLost: function onImageLostFn() {
                   video168017.pause();
               },
               onError: function(errorMessage) {
                   alert(errorMessage);
               }
           });


        // 오션스8
        var video153675 = new AR.VideoDrawable("http://183.111.227.218/files/trailer/153675.mp4", 0.40, {
                   translate: {
                       y: -0.3
                   }
               });

       var tracker153675 = new AR.ImageTrackable(this.tracker, "153675", {
           drawables: {
               cam: [video153675]
           },
           onImageRecognized: function onImageRecognizedFn() {
               if (this.hasVideoStarted) {
                   video153675.resume();
               }
               else {
                   this.hasVideoStarted = true;
                   video153675.play(-1);
               }
               World.removeLoadingBar();
           },
           onImageLost: function onImageLostFn() {
               video153675.pause();
           },
           onError: function(errorMessage) {
               alert(errorMessage);
           }
       });

	},

	removeLoadingBar: function() {
		if (!World.loaded) {
			var e = document.getElementById('loadingMessage');
			e.parentElement.removeChild(e);
			World.loaded = true;
		}
	},

	worldLoaded: function worldLoadedFn() {
		var cssDivInstructions = " style='display: table-cell;vertical-align: middle; text-align: right; width: 50%; padding-right: 15px;'";
		var cssDivSurfer = " style='display: table-cell;vertical-align: middle; text-align: left; padding-right: 15px; width: 38px'";
		var cssDivBiker = " style='display: table-cell;vertical-align: middle; text-align: left; padding-right: 15px;'";
		//document.getElementById('loadingMessage').innerHTML =
         //   "<div" + cssDivInstructions + ">영화 포스터 인식 완료!</div>";
	}
};

World.init();
